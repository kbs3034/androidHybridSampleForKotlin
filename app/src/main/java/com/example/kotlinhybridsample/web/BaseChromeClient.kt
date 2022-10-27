package com.example.kotlinhybridsample.web

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Bitmap
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView

/**
 * 공용(Base) ChromeClient
 */
open class BaseChromeClient
/**
 *
 * @param activity
 */(activity: Activity) : WebChromeClient() {
    protected var targetActivity: Activity = activity

    override fun onJsConfirm(
        view: WebView,
        url: String,
        message: String,
        result: JsResult
    ): Boolean {
        AlertDialog.Builder(
            view.context
        )
            .setTitle("")
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ -> result.confirm() }
            .setNegativeButton(android.R.string.cancel) { _, _ -> result.cancel() }
            .setCancelable(false)
            .create().show()
        return true
    }

    override fun getDefaultVideoPoster(): Bitmap? {
        return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
    }

    companion object {
    }
}