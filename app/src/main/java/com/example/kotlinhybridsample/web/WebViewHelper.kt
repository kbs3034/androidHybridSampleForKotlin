package com.example.kotlinhybridsample.web

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

interface IWebViewDelegate {
    val webView: WebView
    val webViewClient: WebViewClient
    val webChromeClient: WebChromeClient
    val webBridge : WebBridge
}

class WebViewHelper(private val delegate: IWebViewDelegate) {

    fun initWebView() {
        with(delegate) {
            webView.initWebView()

            webView.webViewClient = webViewClient

            webView.webChromeClient = webChromeClient

            webView.addJavascriptInterface(webBridge, WebBridge.DEFAULT_WEB_BRIDGE_NAME)
        }
    }

    fun onWebViewOverrideBackPressed(): Boolean {
        if (delegate.webView.canGoBack()) {
            delegate.webView.goBack()
            return true
        }
        return false
    }
}