package com.example.kotlinhybridsample.web

import android.app.Activity
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.annotation.Keep
import androidx.annotation.MainThread
import com.example.kotlinhybridsample.App
import com.example.kotlinhybridsample.appVersion
import com.example.kotlinhybridsample.base.BaseWebViewActivity
import com.example.kotlinhybridsample.rx.RxBus
import com.example.kotlinhybridsample.rx.RxBusTest
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.net.URLDecoder

open class WebBridge (val callerActivity: BaseWebViewActivity){

    companion object {
        const val DEFAULT_WEB_BRIDGE_NAME = "androidWebBridge"
    }


    private val gson by lazy {
        Gson()
    }

    var sampleHandler: SampleHandler = object : SampleHandler {
        override fun test(args: WebMessageArgs?): JSONObject {
            val returnValue = JSONObject()
            returnValue.put("message", "테스트 브릿지 성공")

            return returnValue
        }

        override fun rxTest(args: WebMessageArgs?) {
            RxBus.instance.publish(RxBusTest("RxTest!!"));
        }
    }

    var nativeSystemHandler: NativeSystemHandler = object : NativeSystemHandler {
        override fun showToast(args: WebMessageArgs?) {
            val message = args?.get("message") as String?
            Toast.makeText(App.INSTANCE, message, Toast.LENGTH_SHORT).show()
        }

        override fun getAppVersion(args: WebMessageArgs?): String {
            return App.INSTANCE.appVersion
        }
    }

    var apiSampleHandler:ApiSampleHandler? = null

    @Keep
    @JavascriptInterface
    fun postMessage(message: String):String {
        Timber.d(message)
        val decoded = URLDecoder.decode(message, "utf-8")
        val webMessage = gson.fromJson(decoded, WebMessage::class.java)
        if (webMessage.group.isBlank())
            return ""

        var resultObject = runFunction(webMessage)

        var result = if(resultObject != null && resultObject is JSONObject) {
            resultObject.toString()
        } else if (resultObject != null && resultObject is String) {
            resultObject
        } else {
            ""
        }

        if(!webMessage.callback.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                callerActivity.webView.runJavascript("${webMessage.callback}('${result}')")
            }
        }

        return result
    }

    @MainThread
    fun runFunction(webMessage: WebMessage):Any? {
        Timber.d("${webMessage.group}.${webMessage.function}(${webMessage.args})")
        Timber.d("callback function name :: ${webMessage.callback}")
        var result:Any? = onWebMessage(webMessage)

        return result;
    }

    private fun onWebMessage(webMessage: WebMessage):Any? {
        var result:Any? = null;

        when (webMessage.group) {
            "sample" -> {
                when (webMessage.function) {
                    "test" -> {
                        result = sampleHandler.test(webMessage.args)
                    }
                    "rxTest" -> {
                        sampleHandler.rxTest(webMessage.args)
                    }
                }
            }
            "nativeSystem" -> {
                when (webMessage.function) {
                    "showToast" -> {
                        nativeSystemHandler?.showToast(webMessage.args)
                    }
                    "appVersion" -> {
                        result = nativeSystemHandler?.getAppVersion(webMessage.args)
                    }
                }
            }
            "ApiSample" -> {
                when (webMessage.function) {
                    "createUser" -> {
                        apiSampleHandler?.createUser(webMessage.args)
                    }
                    "doGetListResources" -> {
                        apiSampleHandler?.doGetListResources(webMessage.args)
                    }
                    "doGetUserList" -> {
                        apiSampleHandler?.doGetUserList(webMessage.args)
                    }
                    "doGetUserListForJsonObject" -> {
                        apiSampleHandler?.doGetUserListForJsonObject(webMessage.args)
                    }
                    "doCreateUserWithField" -> {
                        apiSampleHandler?.doCreateUserWithField(webMessage.args)
                    }
                }
            }
            else -> {
                Toast.makeText(App.INSTANCE, "구현 필요: $webMessage", Toast.LENGTH_SHORT).show()
            }
        }
        return result
    }
}
