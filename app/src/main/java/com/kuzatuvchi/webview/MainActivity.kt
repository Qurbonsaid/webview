package com.kuzatuvchi.webview

import android.app.Activity
import android.annotation.SuppressLint
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.kuzatuvchi.webview.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {
    private val debug = true
    private lateinit var binding: ActivityMainBinding
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null

    companion object {
        private const val BASE_URL = "https://www.google.com"
    }

    private val fileUploadActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val results =
                result.data?.let { WebChromeClient.FileChooserParams.parseResult(result.resultCode, it) }
            fileUploadCallback?.onReceiveValue(results)
            fileUploadCallback = null
        }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWebView()
        setupSwipeRefresh()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    private fun setupWebView() {
        val webView = binding.webView

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            builtInZoomControls = false
            displayZoomControls = false
            textZoom = 100
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.apply {
            setOnLongClickListener { true }
            isHapticFeedbackEnabled = false
            webViewClient = CustomWebViewClient()
            webChromeClient = CustomWebChromeClient()
        }

        if (isNetworkAvailable()) {
            webView.loadUrl(BASE_URL)
        } else {
            webView.loadDataWithBaseURL("about:blank", customErrorHtml, "text/html", "utf-8", null)
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            val currentUrl = binding.webView.url ?: BASE_URL
            if (isNetworkAvailable()) {
                binding.webView.loadUrl(currentUrl)
            } else {
                binding.webView.loadDataWithBaseURL("about:blank", customErrorHtml, "text/html", "utf-8", null)
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private inner class CustomWebViewClient : WebViewClient() {
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                showToast("Error: ${error?.description}, URL: ${request?.url}")
            }
            view?.loadDataWithBaseURL("about:blank", customErrorHtml, "text/html", "utf-8", null)
        }
    }

    private inner class CustomWebChromeClient : WebChromeClient() {
        @Suppress("DEPRECATION")
        @Deprecated("Deprecated in Java")
        override fun onConsoleMessage(message: String?, lineNumber: Int, sourceID: String?) {
            showToast("$message -- From line $lineNumber of $sourceID")
        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            fileUploadCallback = filePathCallback ?: return false
            val intent = fileChooserParams?.createIntent()
            fileUploadActivityResultLauncher.launch(intent)
            return true
        }
    }

    private fun showToast(message: String) {
        if (debug) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        } else {
            Log.d(localClassName, message)
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            } ?: false
        } else {
            connectivityManager.activeNetworkInfo?.isConnected == true
        }
    }

    private val customErrorHtml =
            """<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Document</title>
  </head>
  <style>
        .container{
            height: 100vh;
            padding: 7px;
            padding-top: 100px;
        }
        .wifi-offIcon{
            font-size: 50px;
            text-align: center;
        }
        .title{
            font-family: sans-serif;
            text-align: center;
        }
        .description{
            font-family: sans-serif;
            text-align: center;
            color: gray;
        }
        .btn{
            width: 100%;
            height: 7%;
            border: none;
            color: white;
            font-family: sans-serif;
            border-radius: 10px;
            font-size: 18px;
            background-color: #1678FF;
        }
  </style>
  <body>
        <div class="container">
                <p class="wifi-offIcon"><svg stroke="currentColor" fill="currentColor" stroke-width="0" viewBox="0 0 24 24" height="1em" width="1em" xmlns="http://www.w3.org/2000/svg"><path fill="none" d="M0 0h24v24H0V0z"></path><path d="m21 11 2-2c-3.73-3.73-8.87-5.15-13.7-4.31l2.58 2.58c3.3-.02 6.61 1.22 9.12 3.73zm-2 2a9.895 9.895 0 0 0-3.72-2.33l3.02 3.02.7-.69zM9 17l3 3 3-3a4.237 4.237 0 0 0-6 0zM3.41 1.64 2 3.05 5.05 6.1C3.59 6.83 2.22 7.79 1 9l2 2c1.23-1.23 2.65-2.16 4.17-2.78l2.24 2.24A9.823 9.823 0 0 0 5 13l2 2a6.999 6.999 0 0 1 4.89-2.06l7.08 7.08 1.41-1.41L3.41 1.64z"></path></svg></p>
                <h2 class="title">Internet ulanish  mavjud <br /> emas.</h2>
                <p class="description">Wi-Fi yoki mobil tarmoqqa ulanishin tekshiring va <br /> qaytadan urining.</p>
                <button class="btn" onclick="window.location.replace('${BASE_URL}')">Qayta urinish</button>
        </div>
  </body>
</html>""".trimIndent()

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
