package com.example.kotlinhybridsample


import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.kotlinhybridsample.base.BaseWebViewActivity
import com.example.kotlinhybridsample.databinding.ActivityMainBinding
import com.example.kotlinhybridsample.rx.IEventBusSubscription
import com.example.kotlinhybridsample.rx.RxBus
import com.example.kotlinhybridsample.rx.RxBusTest
import com.example.kotlinhybridsample.web.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import timber.log.Timber
import java.io.IOException

class MainActivity : BaseWebViewActivity(R.layout.activity_main) {

    private val binding: ActivityMainBinding by viewBinding()
    private val mainViewModel:MainViewModel by viewModels()

    private lateinit var rxBusTestSubscription: IEventBusSubscription

    override val webView: WebView
        get() = binding.webView

    override val webChromeClient: WebChromeClient
        get() = BaseChromeClient(this)

    override val webViewClient: WebViewClient
        get() = MainWebViewClient(this)

    override val webBridge = WebBridge(this).apply {
        apiSampleHandler = object : ApiSampleHandler {
            override fun createUser(args: WebMessageArgs?) {
                mainViewModel.createUser {
                    if(it != null)apiTestCallback(toJson(it).toString())
                }
            }

            override fun doGetListResources(args: WebMessageArgs?) {
                mainViewModel.doGetListResouces {
                    if(it != null)apiTestCallback(toJson(it).toString())
                }
            }

            override fun doGetUserList(args: WebMessageArgs?) {
                var page = args?.get("page") as String
                mainViewModel.doGetUserList(page) {
                    if(it != null) apiTestCallback(it.toString())
                }
            }

            override fun doGetUserListForJsonObject(args: WebMessageArgs?) {
                var page = args?.get("page") as String
                mainViewModel.doGetUserListForJsonObject(page) {
                    if(it != null)apiTestCallback(toJson(it).toString())
                }
            }

            override fun doCreateUserWithField(args: WebMessageArgs?) {
                var name = args?.get("name") as String
                var job = args?.get("job") as String
                mainViewModel.doCreateUserWithField(name, job) {
                    if(it != null)apiTestCallback(toJson(it).toString())
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView.loadUrl("file:///android_asset/sample.html")

        rxBusTestSubscription = RxBus.instance.subscribe(RxBusTest::class.java) {
            webView.runJavascript("alert('${it.message}')")
        }

    }


    inner class MainWebViewClient(activity: Activity) : BaseWebViewClient(activity, this) {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            val uri = Uri.parse(url)

            setStatusBar(uri)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
        }

        private fun setStatusBar(uri: Uri) {
            if (uri.path == "test") {
                window.statusBarColor = this@MainActivity.getColor(R.color.black)
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else {
                window.statusBarColor = Color.WHITE
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    override fun handleHttpUrl(url: String): Boolean {
        Timber.d("mainWebView URL ::: $url")
        return false
    }

    private fun apiTestCallback(str:String){
        CoroutineScope(Dispatchers.Main).launch {
            webView.runJavascript("alert('$str')")
        }
    }

    private fun toJson(response: ResponseBody): JSONObject {
        val result: JSONObject = try {
            JSONObject(response!!.string())
        } catch (e: IOException) {
            e.printStackTrace()
            JSONObject()
        } catch (e: JSONException) {
            e.printStackTrace()
            JSONObject()
        }
        return result
    }

}