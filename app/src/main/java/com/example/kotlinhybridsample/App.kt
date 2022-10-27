package com.example.kotlinhybridsample

import LogController
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.webkit.WebView
import android.widget.Toast
import com.orhanobut.logger.*
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.io.IOException
import java.net.SocketException

class App : Application() {

    companion object {
        lateinit var INSTANCE: App
    }

    init {
        INSTANCE = this
    }

    override fun onCreate() {
        super.onCreate()
        setupTimber()

        val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(true) // (Optional) Whether to show thread info or not. Default true
            .methodCount(1) // (Optional) How many method line to show. Default 2
            .methodOffset(5) // Set methodOffset to 5 in order to hide internal method calls
            .tag("Timber") // To replace the default PRETTY_LOGGER tag with a dash (-).
            .build()

        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })

        if (BuildConfig.DEBUG) {
            LogController.initialize(this)
        }

        setupWebViewDebugIfNeeded()

        Timber.d("App Started")
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(object : DebugTree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    Logger.log(priority, tag, message, t)
                }
            })
        }
    }

    private fun setupWebViewDebugIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName(this)
            val packageName = applicationContext.packageName //PROCESS
            if (packageName != processName) {
                processName?.let {
                    WebView.setDataDirectorySuffix(it)
                }
            }
        }

        if (BuildConfig.FLAVOR != "prd" || BuildConfig.DEBUG) {
            try {
                WebView.setWebContentsDebuggingEnabled(true)
            } catch (e: Exception) {
                Timber.e(e.stackTraceToString())
            }
        }
    }

    private fun getProcessName(context: Context?): String? {
        if (context == null) return null
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (processInfo in manager.runningAppProcesses) {
            if (processInfo.pid == Process.myPid()) {
                return processInfo.processName
            }
        }
        return null
    }
}

/**
 * App version
 *
 * @return
 */
val Context.appVersion: String
    get() {
        var versionName = ""
        try {
            val info = packageManager.getPackageInfo(packageName, 0)
            versionName = info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return versionName
    }