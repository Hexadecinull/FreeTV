package io.github.ssmg4.freetv.player

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import io.github.ssmg4.freetv.R

class HTMLActivity : AppCompatActivity() {

    private var wvHtml: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_html)

        val link = intent.getStringExtra("Url")

        wvHtml = findViewById<View>(R.id.wvHtml) as WebView
        wvHtml!!.webViewClient = WebViewClient()
        wvHtml!!.webChromeClient = MyChrome()
        wvHtml!!.settings.apply {
            javaScriptEnabled = true
            mediaPlaybackRequiresUserGesture = false
        }

        if (savedInstanceState == null) {
            wvHtml!!.loadUrl(link.toString())
        }

        // Fix deprecated onBackPressed
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (wvHtml?.canGoBack() == true) {
                    wvHtml?.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private inner class MyChrome : WebChromeClient() {
        private var customView: View? = null
        private var customViewCallback: CustomViewCallback? = null
        private var originalOrientation = 0
        private var originalSystemUiVisibility = 0

        override fun getDefaultVideoPoster(): Bitmap? =
            if (customView == null) null
            else BitmapFactory.decodeResource(applicationContext.resources, 2130837573)

        override fun onHideCustomView() {
            (window.decorView as FrameLayout).removeView(customView)
            customView = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.show(WindowInsets.Type.systemBars())
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = originalSystemUiVisibility
            }
            requestedOrientation = originalOrientation
            customViewCallback?.onCustomViewHidden()
            customViewCallback = null
        }

        override fun onShowCustomView(paramView: View, paramCustomViewCallback: CustomViewCallback) {
            if (customView != null) { onHideCustomView(); return }
            customView = paramView
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                originalSystemUiVisibility = 0
                window.insetsController?.let {
                    it.hide(WindowInsets.Type.systemBars())
                    it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                originalSystemUiVisibility = window.decorView.systemUiVisibility
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            }
            originalOrientation = requestedOrientation
            customViewCallback = paramCustomViewCallback
            (window.decorView as FrameLayout).addView(customView, FrameLayout.LayoutParams(-1, -1))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        wvHtml?.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        wvHtml?.restoreState(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        wvHtml?.stopLoading()
        wvHtml?.destroy()
    }
}
