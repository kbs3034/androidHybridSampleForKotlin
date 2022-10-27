import android.annotation.SuppressLint
import android.content.Context
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPOutputStream

typealias LogElement = Triple<String, Int, String?>

object LogController {

    private var flush = BehaviorSubject.create<Long>()
    private var flushCompleted = BehaviorSubject.create<Long>()

    private var LOG_LEVELS = arrayOf("", "", "VERBOSE",
            "DEBUG",
            "INFO",
            "WARN",
            "ERROR",
            "ASSERT")

    /**
     * ~1.66MB/~450kb gzipped.
     */
    private const val LOG_FILE_MAX_SIZE_THRESHOLD = 5 * 1024 * 1024
    private val LOG_FILE_RETENTION = TimeUnit.DAYS.toMillis(14)
    private val LOG_FILE_TIME_FORMAT = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
    val LOG_LINE_TIME_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    private lateinit var filePath: String
    private const val LOG_FILE_NAME = "insights.log"

    fun initialize(context: Context) {
        filePath = try {
            getLogsDirectoryFromPath(context.filesDir.absolutePath)
        } catch (e: FileNotFoundException) {
            // Fallback to default path
            context.filesDir.absolutePath
        }

        Timber.plant(FileTree())
    }

    @SuppressLint("CheckResult")
    class FileTree : Timber.Tree() {

        private val logBuffer = PublishSubject.create<LogElement>()

        init {
            var processed = 0

            logBuffer.observeOn(Schedulers.computation())
                    .doOnEach {
                        processed++

                        if (processed % 20 == 0) {
                            flush()
                        }
                    }
                    .buffer(flush.mergeWith(Observable.interval(5, TimeUnit.MINUTES)))
                    .subscribeOn(Schedulers.io())
                    .subscribe {
                        try {
                            // Open file
                            val f = getFile(filePath, LOG_FILE_NAME)

                            // Write to log
                            FileWriter(f, true).use { fw ->
                                // Write log lines to the file
                                it.forEach { (date, priority, message) -> fw.append("$date\t${LOG_LEVELS[priority]}\t$message\n") }

                                // Write a line indicating the number of log lines proceed
                                fw.append("${LOG_LINE_TIME_FORMAT.format(Date())}\t${LOG_LEVELS[2] /* Verbose */}\tFlushing logs -- total processed: $processed\n")

                                fw.flush()
                            }

                            // Validate file size
                            flushCompleted.onNext(f.length())
                        } catch (e: Exception) {
                            logException(e)
                        }
                    }

            flushCompleted
                    .subscribeOn(Schedulers.io())
                    .filter { filesize -> filesize > LOG_FILE_MAX_SIZE_THRESHOLD }
                    .subscribe { rotateLogs() }
        }

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            logBuffer.onNext(LogElement(LOG_LINE_TIME_FORMAT.format(Date()), priority, message))
        }
    }

    fun flush(oncomplete: (() -> Unit)? = null) {
        oncomplete?.run {
            Timber.w("Subscribing to flush completion handler")

            flushCompleted
                    .take(1)
                    .timeout(2, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .onErrorReturn { -1L }
                    .filter { it > 0 }
                    .subscribe {
                        rotateLogs()

                        // Delegate back to caller
                        oncomplete()
                    }
        }

        flush.onNext(1L)
    }

    fun rotateLogs() {
        rotateLogs(filePath, LOG_FILE_NAME)
    }

    private fun rotateLogs(path: String, name: String) {
        val file = getFile(path, name)

        if (!compress(file)) {
            // Unable to compress file
            return
        }

        // Truncate the file to zero.
        PrintWriter(file).close()

        // Iterate over the gzipped files in the directory and delete the files outside the
        // retention period.
        val currentTime = System.currentTimeMillis()
        file.parentFile.listFiles()
                ?.filter {
                    it.extension.toLowerCase(Locale.ROOT) == "gz"
                            && it.lastModified() + LOG_FILE_RETENTION < currentTime
                }?.map { it.delete() }
    }

    private fun getLogsDirectoryFromPath(path: String): String {

        val dir = File(path, "logs")

        if (!dir.exists() && !dir.mkdirs()) {
            throw FileNotFoundException("Unable to create logs file")
        }

        return dir.absolutePath
    }

    private fun getFile(path: String, name: String): File {
        val file = File(path, name)

        if (!file.exists() && !file.createNewFile()) {
            throw IOException("Unable to load log file")
        }

        if (!file.canWrite()) {
            throw IOException("Log file not writable")
        }

        return file
    }

    private fun compress(file: File): Boolean {
        try {
            val compressed = File(file.parentFile.absolutePath, "${file.name.substringBeforeLast(".")}_${LOG_FILE_TIME_FORMAT.format(Date())}.gz")

            FileInputStream(file).use { fis ->
                FileOutputStream(compressed).use { fos ->
                    GZIPOutputStream(fos).use { gzos ->

                        val buffer = ByteArray(1024)
                        var length = fis.read(buffer)

                        while (length > 0) {
                            gzos.write(buffer, 0, length)

                            length = fis.read(buffer)
                        }

                        // Finish file compressing and close all streams.
                        gzos.finish()
                    }
                }
            }
        } catch (e: IOException) {
            logException(e)

            return false
        }

        return true
    }

    private fun logException(e: Exception) {
        Timber.e(e.stackTraceToString())
    }
}