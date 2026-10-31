package com.jaisalmer.desertledger

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.*

/** A thin Android client: the deployed Apps Script remains the single source of truth. */
class MainActivity : Activity() {
    companion object {
        const val WEB_APP_URL = "https://script.google.com/macros/s/AKfycbxVk-ghCoZW2GYFriYEZum9Zl3rNPvC5fmOCOaDZxckr4MdGUfv2qfKR0l-7ScGwa0q/exec"
    }
    private lateinit var web: WebView
    private lateinit var progress: ProgressBar
    private lateinit var errorPanel: LinearLayout
    private var failed = false
    private var pendingJs: JsResult? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(33,23,16)) }
        root.setOnApplyWindowInsetsListener { v, insets ->
            @Suppress("DEPRECATION")
            v.setPadding(insets.systemWindowInsetLeft,insets.systemWindowInsetTop,insets.systemWindowInsetRight,insets.systemWindowInsetBottom)
            insets
        }
        val bar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(12,4,12,4) }
        val title = TextView(this).apply { text = "Desert Ledger"; textSize = 19f; setTextColor(Color.rgb(232,200,137)); gravity = android.view.Gravity.CENTER_VERTICAL }
        bar.addView(title, LinearLayout.LayoutParams(0,56,1f))
        bar.addView(Button(this).apply { text = "⋮"; contentDescription = "App options"; setOnClickListener { showMenu() } })
        root.addView(bar)
        progress = ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply { max = 100 }
        root.addView(progress,LinearLayout.LayoutParams(-1,6))
        val frame = FrameLayout(this)
        root.addView(frame,LinearLayout.LayoutParams(-1,0,1f))
        web = WebView(this)
        frame.addView(web,FrameLayout.LayoutParams(-1,-1))
        errorPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = android.view.Gravity.CENTER
            setPadding(32,32,32,32); setBackgroundColor(Color.rgb(33,23,16)); visibility = View.GONE
        }
        errorPanel.addView(TextView(this).apply {
            text = "Could not load your ledger.\nCheck your internet connection and try again."
            textSize = 17f; gravity = android.view.Gravity.CENTER; setTextColor(Color.rgb(232,200,137))
        })
        errorPanel.addView(Button(this).apply { text = "Retry"; setOnClickListener { loadHome() } })
        errorPanel.addView(Button(this).apply { text = "Open in Chrome / Browser"; setOnClickListener { external(Uri.parse(WEB_APP_URL)) } })
        frame.addView(errorPanel,FrameLayout.LayoutParams(-1,-1))
        setContentView(root)
        root.requestApplyInsets()
        web.setBackgroundColor(Color.rgb(33,23,16))
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = false
        }
        CookieManager.getInstance().setAcceptCookie(true)
        // Apps Script renders the form within a Google-hosted iframe.
        CookieManager.getInstance().setAcceptThirdPartyCookies(web,true)
        web.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, value: Int) {
                progress.progress = value
                progress.visibility = if(value < 100 && !failed) View.VISIBLE else View.GONE
            }
            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult): Boolean {
                pendingJs?.cancel(); pendingJs = result
                AlertDialog.Builder(this@MainActivity).setTitle("Confirm")
                    .setMessage(message)
                    .setPositiveButton("Confirm") { _,_ -> result.confirm(); pendingJs = null }
                    .setNegativeButton("Cancel") { _,_ -> result.cancel(); pendingJs = null }
                    .setOnCancelListener { result.cancel(); pendingJs = null }.show()
                return true
            }
            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult): Boolean {
                AlertDialog.Builder(this@MainActivity).setMessage(message)
                    .setPositiveButton("OK") { _,_ -> result.confirm() }
                    .setOnCancelListener { result.cancel() }.show()
                return true
            }
        }
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean {
                val uri = request.url
                val host = uri.host?.lowercase().orEmpty()
                if(uri.scheme == "https" && (host == "script.google.com" || host == "script.googleusercontent.com" || host.endsWith(".googleusercontent.com"))) return false
                if(request.isForMainFrame && uri.scheme in listOf("https","http","mailto","tel")) {
                    external(uri)
                    return true
                }
                return true
            }
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                failed = false; errorPanel.visibility = View.GONE
            }
            override fun onPageFinished(view: WebView?, url: String?) { CookieManager.getInstance().flush() }
            override fun onReceivedError(view: WebView?, request: WebResourceRequest, error: WebResourceError?) {
                if(request.isForMainFrame) showError()
            }
            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest, response: WebResourceResponse?) {
                if(request.isForMainFrame && (response?.statusCode ?: 0) >= 400) showError()
            }
            // SSL errors use WebView's default cancellation. Never bypass certificate checks.
        }
        if(savedInstanceState == null || web.restoreState(savedInstanceState) == null) loadHome()
    }
    private fun showError() { failed = true; progress.visibility = View.GONE; errorPanel.visibility = View.VISIBLE }
    private fun loadHome() { failed = false; errorPanel.visibility = View.GONE; web.loadUrl(WEB_APP_URL) }
    private fun external(uri: Uri) {
        try { startActivity(Intent(Intent.ACTION_VIEW,uri)) }
        catch (_: Exception) { Toast.makeText(this,"No browser available",Toast.LENGTH_LONG).show() }
    }
    private fun confirmReload(action: () -> Unit) {
        AlertDialog.Builder(this).setTitle("Reload ledger?")
            .setMessage("Any unsaved form details will be lost.")
            .setPositiveButton("Reload") { _,_ -> action() }.setNegativeButton("Cancel",null).show()
    }
    private fun showMenu() {
        AlertDialog.Builder(this).setTitle("Desert Ledger")
            .setItems(arrayOf("Reload website","Open in Chrome / Browser","Clear website session")) { _,which ->
                when(which) {
                    0 -> confirmReload { loadHome() }
                    1 -> external(Uri.parse(WEB_APP_URL))
                    2 -> AlertDialog.Builder(this).setTitle("Clear this device's session?")
                        .setMessage("Unsaved form details will be lost. Your saved Google Sheet records will remain.")
                        .setPositiveButton("Clear") { _,_ ->
                            CookieManager.getInstance().removeAllCookies {
                                WebStorage.getInstance().deleteAllData(); web.clearCache(true); web.clearHistory(); loadHome()
                            }
                        }.setNegativeButton("Cancel",null).show()
                }
            }.show()
    }
    @Deprecated("Uses the platform back callback for this single WebView activity")
    override fun onBackPressed() {
        // Apps Script's internal panels are JS state, not Android browser history.
        AlertDialog.Builder(this).setTitle("Close app?")
            .setMessage("Use the website's Back/Home buttons for forms. Unsaved details are lost when closing.")
            .setPositiveButton("Close") { _,_ -> finish() }.setNegativeButton("Stay",null).show()
    }
    override fun onSaveInstanceState(outState: Bundle) { web.saveState(outState); super.onSaveInstanceState(outState) }
    override fun onPause() { web.onPause(); super.onPause() }
    override fun onResume() { super.onResume(); if(::web.isInitialized) web.onResume() }
    override fun onDestroy() { pendingJs?.cancel(); pendingJs = null; web.stopLoading(); web.destroy(); super.onDestroy() }
}
