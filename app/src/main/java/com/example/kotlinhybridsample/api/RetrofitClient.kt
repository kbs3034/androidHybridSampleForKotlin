package com.example.kotlinhybridsample.api

import com.example.kotlinhybridsample.BuildConfig
import com.example.kotlinhybridsample.api.data.sample.SampleUser
import com.example.kotlinhybridsample.api.data.sample.SampleUserList
import com.example.kotlinhybridsample.web.UserAgentManager
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit


inline fun <reified T> Observable<T>.retryWithDelay(
    maxRetries: Int,
    retryDelayMillis: Int
): Observable<T> {
    var retryCount = 0

    return retryWhen { thObservable ->
        thObservable.flatMap { throwable ->
            if (++retryCount < maxRetries) {
                Timber.d("retryWhen ${T::class.java} retry($retryCount) ...")
                Observable.timer(retryDelayMillis.toLong(), TimeUnit.MILLISECONDS)
            } else {
                Timber.d("retryWhen ${T::class.java} retry($retryCount) failed")
                Observable.error(throwable)
            }
        }
    }
}

inline fun <reified T> applySchedulersAndRetryPolicy(
    maxRetries: Int = 3,
    retryDelayMillis: Int = 1000
): ObservableTransformer<T, T> {
    return ObservableTransformer<T, T> { upstream ->
        upstream
            .retryWithDelay(maxRetries, retryDelayMillis)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

const val API_TYPE_USER = "user"
const val API_TYPE_MYDATA = "mydata"

class RetrofitClient {

    private fun retrofitBuild(): Retrofit {
        return Retrofit.Builder()
            .baseUrl( BuildConfig.apiUrl )
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(createOkHttpClient())
            .build()
    }

//    private var bitnineService = retrofitBuild().create(BitnineService::class.java)

    companion object {
        private val retrofitClient: RetrofitClient = RetrofitClient()

        fun getInstance(): RetrofitClient {
            return retrofitClient
        }

        const val MAX_RETRIES = 3
        const val RETRY_DELAY_MILLIS = 1000
    }

    fun getService(): SampleService = retrofitBuild().create(SampleService::class.java)

    /**
     * 통신 로그 확인용 OkHttpClient 생성
     */
    private fun createOkHttpClient(): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        } else {
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder().apply {
            addNetworkInterceptor(httpLoggingInterceptor)
            addInterceptor(interceptorChain())
        }
            .readTimeout(1, TimeUnit.MINUTES)
            .connectTimeout(1, TimeUnit.MINUTES)
            .build()
    }

    private fun interceptorChain(): Interceptor = Interceptor { chain ->
        val builder = chain.request().newBuilder()
            .header("User-Agent", UserAgentManager.userAgent)

        chain.proceed(builder.build())
    }


    fun createUserObservable(user: SampleUser): Observable<ResponseBody> {
        return getService().createUser(user).compose(applySchedulersAndRetryPolicy())
    }

    fun doGetListResoucesObservable(): Observable<ResponseBody> {
        return getService().doGetListResouces().compose(applySchedulersAndRetryPolicy())
    }

    fun doGetUserListObservable(page:String): Observable<SampleUserList> {
        return getService().doGetUserList(page).compose(applySchedulersAndRetryPolicy())
    }

    fun doGetUserListForJsonObjectObservable(page:String): Observable<ResponseBody> {
        return getService().doGetUserListForJsonObject(page).compose(applySchedulersAndRetryPolicy())
    }

    fun doCreateUserWithFieldObservable(name:String, job:String): Observable<ResponseBody> {
        return getService().doCreateUserWithField(name, job).compose(applySchedulersAndRetryPolicy())
    }

}