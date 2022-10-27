package com.example.kotlinhybridsample.web

import android.os.Build
import android.text.TextUtils
import android.webkit.WebSettings
import com.example.kotlinhybridsample.App
import com.example.kotlinhybridsample.appVersion
import timber.log.Timber

/**
 * API or WebView UserAgent Manager
 */
object UserAgentManager {

    private val defaultUserAgent by lazy {
        try {
            WebSettings.getDefaultUserAgent(App.INSTANCE)
        } catch (e: Exception) {
            Timber.e(e.stackTraceToString())
            CUSTOM_USER_AGENT
        }
    }

    private var mUserAgent: String = ""

    val userAgent: String
        get() = if (TextUtils.isEmpty(mUserAgent)) {
            val newUserAgent = defaultUserAgent + userAgentExtra
            mUserAgent = newUserAgent
            newUserAgent
        } else {
            mUserAgent
        }

    private val userAgentExtra: String
        get() {
            val app: App = App.INSTANCE
            return ("$SECTION_DELIMITER$APP_ID"
                    + "$DELIMITER${app.appVersion}$DELIMITER${Build.MODEL}")
        }

    private const val APP_ID = "runable"
    private const val DELIMITER = "/"
    private const val SECTION_DELIMITER = " "
    private const val CUSTOM_USER_AGENT =
        "Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.183 Mobile Safari/537.36"
}