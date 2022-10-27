package com.example.kotlinhybridsample.base

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinhybridsample.web.IDefaultWebViewClientStrategy
import com.example.kotlinhybridsample.web.IWebViewDelegate
import com.example.kotlinhybridsample.web.WebViewHelper
import timber.log.Timber

abstract class BaseWebViewActivity(@LayoutRes contentLayoutId: Int = 0) :
    AppCompatActivity(contentLayoutId), IWebViewDelegate, IDefaultWebViewClientStrategy {

    protected var isNeedReloadOnResume: Boolean = false

    final override val webViewActivity: Activity
        get() = this

    protected val webViewHelper by lazy { WebViewHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val request = DownloadManager.Request(Uri.parse(url))
            var fileNm = URLUtil.guessFileName(url, contentDisposition, mimeType)

            if (url.contains("?fileNm=")) {
                fileNm = url.split("?fileNm=")[1]
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/runable/GPX/${fileNm}")
            } else {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileNm)
            }

            request.setMimeType(mimeType)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setAllowedOverRoaming(false)
            request.addRequestHeader("cookie", CookieManager.getInstance().getCookie(url))
            request.addRequestHeader("User-Agent", userAgent)
            request.setTitle(fileNm)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            Toast.makeText(this, "다운로드가 완료되었습니다.\n" +
                                            "'내 파일/내장 메모리/Download/runable'에서 확인해보세요.", Toast.LENGTH_LONG).show()


            var downloadId = downloadManager.enqueue(request)
            Timber.d("path :   ${downloadId}")
        }

        webViewHelper.initWebView()
    }

    override fun onResume() {
        super.onResume()
        if (isNeedReloadOnResume) {
            webView.reload()
            isNeedReloadOnResume = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}