package com.example.kotlinhybridsample.web

import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.annotation.MainThread

fun WebView.initWebView() {
    scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
    with(settings) {
        javaScriptEnabled = true //자바스크립트 허용
        loadWithOverviewMode = true
        useWideViewPort = true
        databaseEnabled = true
        domStorageEnabled = true
        allowFileAccess = true
        javaScriptCanOpenWindowsAutomatically = true
        cacheMode = WebSettings.LOAD_NO_CACHE
        textZoom = 100
        
        userAgentString = UserAgentManager.userAgent
    }

    val webCookieManager = CookieManager.getInstance()
    webCookieManager.setAcceptCookie(true)

    // SSL 처리
    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
    webCookieManager.setAcceptThirdPartyCookies(this, true)
}

/**
 * Add WebBridge
 */
fun WebView.addWebViewBridge(
    webBridge: WebBridge,
    bridgeName: String = WebBridge.DEFAULT_WEB_BRIDGE_NAME
) {
    addJavascriptInterface(webBridge, bridgeName)
}

@MainThread
fun WebView.runJavascript(script: String, resultCallback: ValueCallback<String>? = null) {
    evaluateJavascript(script, resultCallback)
}