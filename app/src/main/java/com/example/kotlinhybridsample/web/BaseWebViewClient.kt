package com.example.kotlinhybridsample.web

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.webkit.*
import androidx.annotation.RequiresApi
import timber.log.Timber

open class BaseWebViewClient(
    protected val context: Context,
    private val strategy: IWebViewClientStrategy
) : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        Timber.d("onPageStarted $url")
        startedUrl = url
    }

    private var startedUrl: String? = null

    private fun WebResourceRequest.toStr(): String {
        var str = "method:${method}, url:${url}, isForMainFrame:${isForMainFrame}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            str += ", isRedirect:${isRedirect}"
        }
        return str
    }

    @TargetApi(Build.VERSION_CODES.N)
    final override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        Timber.d("24 shouldOverrideUrlLoading ${request?.toStr()}")
        return handleRequest(view, request, request?.url.toString())
    }

    @Suppress("DEPRECATION")
    final override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        Timber.d("shouldOverrideUrlLoading $url")
        return handleRequest(view, null, url)
    }

    private fun handleRequest(view: WebView?, request: WebResourceRequest?, url: String?): Boolean =
        strategy.handleRequest(view, request, url)

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Timber.d("onPageFinished $url")
    }

    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        super.onReceivedHttpError(view, request, errorResponse)
        Timber.e("${request?.toStr()}\nstatusCode:${errorResponse?.statusCode}")
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        request?.let {
            if (it.method == "POST" && it.isForMainFrame) {
                Timber.d(it.toStr())
            }
        }
        return super.shouldInterceptRequest(view, request)
    }

}