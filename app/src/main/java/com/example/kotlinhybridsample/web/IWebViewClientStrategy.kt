package com.example.kotlinhybridsample.web

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.annotation.RequiresApi
import timber.log.Timber
import java.net.URISyntaxException

interface IWebViewClientStrategy {
    fun handleRequest(view: WebView?, request: WebResourceRequest?, url: String?): Boolean
    fun handleHttpUrl(url: String): Boolean
}

interface IDefaultWebViewClientStrategy : IWebViewClientStrategy {

    val webViewActivity: Activity

    override fun handleRequest(
        view: WebView?,
        request: WebResourceRequest?,
        url: String?
    ): Boolean {
        return handleRequestDefault(view, request, url)
    }

    private fun handleRequestDefault(
        view: WebView?,
        request: WebResourceRequest?,
        url: String?
    ): Boolean {
        return url?.let {
            if (url.startsWith("tel:") ||
                url.startsWith("mailto:") ||
                url.startsWith("sms:") ||
                url.startsWith("smsto:") ||
                url.startsWith("mms:") ||
                url.startsWith("mmsto:")
            ) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                webViewActivity.startActivity(intent)
                return true
            }

            return if (!url.startsWith("http")) {
                handleOtherAppCall(url)
            } else {
                handleHttpUrl(url)
            }
        } ?: false
    }

    private fun handleOtherAppCall(url: String): Boolean {
        var intent: Intent

        intent = try {
            Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
        } catch (ex: URISyntaxException) {
            Timber.e("Bad URI " + url + ":" + ex.message)
            return false
        }

        val returnFlag =
            if (url.startsWith("intent")) { // chrome 버젼 방식
                // 앱설치 체크
                if (webViewActivity.packageManager.resolveActivity(intent, 0) == null) {
                    val packageName = intent.getPackage()
                    if (packageName != null) {
                        val uri = Uri.parse("market://search?q=pname:$packageName")
                        intent = Intent(Intent.ACTION_VIEW, uri)
                        try {
                            webViewActivity.startActivity(intent)
                            true
                        } catch (e: ActivityNotFoundException) {
                            Timber.e(e, e.localizedMessage)
                            false
                        }
                    } else {
                        false
                    }
                } else {
                    intent.addCategory(Intent.CATEGORY_BROWSABLE)
                    intent.component = null
                    try {
                        webViewActivity.startActivityIfNeeded(intent, -1)
                        true
                    } catch (e: ActivityNotFoundException) {
                        Timber.e(e, e.localizedMessage)
                        false
                    }
                }
            } else { // 구 방식
                val uri = Uri.parse(url)
                intent = Intent(Intent.ACTION_VIEW, uri)
                try {
                    webViewActivity.startActivity(intent)
                    true
                } catch (e: ActivityNotFoundException) {
                    Timber.e(e, e.localizedMessage)
                    true // http, https 가 아니므로 처리할 수 없는 scheme 이라 아무 동작하지 않도록 true 를 반환함
                }
            }

        Timber.d("[handleOtherAppCall] url=$url, $returnFlag")

        return returnFlag
    }

    private fun WebResourceRequest.toStr(): String {
        var str = "method:${method}, url:${url}, isForMainFrame:${isForMainFrame}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            str += ", isRedirect:${isRedirect}"
        }
        return str
    }

}