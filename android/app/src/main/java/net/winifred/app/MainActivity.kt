package net.winifred.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView

/* ================================================================
   Winifred — the phone app
   ================================================================
   She is a web app: this is a shell around one address. That is
   deliberate, and it is the same decision as Hattie's, in reverse —
   Hattie talks to an HTTP API because her face is native; Winifred's
   conversation, sign-in and settings are already built as pages, so
   wrapping them is one place to fix a bug instead of two.

   WHAT THIS FILE IS RESPONSIBLE FOR

   1. Loading https://winifred-ai.net/app/ — the same origin as her
      PHP, so the session cookie is first-party and "keep me signed
      in" behaves exactly as it does in a browser.
   2. Keeping the conversation in the shell: only her own host loads
      inside. The PayPal checkout and any link she quotes open in the
      real browser, because card pages must not be rendered inside an
      app we cannot vouch for.
   3. The back button walking the history rather than quitting on the
      first press, which is the difference between an app and a
      website in a window.
   4. Saying plainly when she cannot be reached, with a way to try
      again, instead of the browser's grey "webpage not available".
   ================================================================ */

class MainActivity : Activity() {

    private companion object {
        const val START_URL = "https://winifred-ai.net/app/"
        const val APP_HOST = "winifred-ai.net"

        /* Her palette: --blue-0, so the first frame is already the app's. */
        const val NAVY = "#0D1B2A"
    }

    private lateinit var web: WebView
    private lateinit var trouble: TextView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        web = WebView(this)
        web.setBackgroundColor(Color.parseColor(NAVY))

        with(web.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            /* If her voice is ever wired up, it must be allowed to speak
               without a tap first. Costs nothing while it is unused. */
            mediaPlaybackRequiresUserGesture = false
            /* A marker the server can see, if it ever needs to tell the
               app apart from a browser tab. */
            userAgentString = "$userAgentString WinifredApp/1.0"
        }

        val cookies = CookieManager.getInstance()
        cookies.setAcceptCookie(true)
        /* So the day the app is served from another host, the session
           still follows it. Harmless while she is first-party. */
        cookies.setAcceptThirdPartyCookies(web, true)

        web.webViewClient = object : WebViewClient() {

            /* Only her host stays inside. Everything else — PayPal, a news
               article she quotes — belongs in a browser. */
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url
                val host = url.host ?: return false
                val ours = host == APP_HOST || host.endsWith(".$APP_HOST")
                if (ours) return false
                openOutside(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                trouble.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                /* Only the page itself, not a picture or a font inside it. */
                if (!request.isForMainFrame) return
                trouble.visibility = View.VISIBLE
            }
        }

        trouble = TextView(this).apply {
            setTextColor(Color.parseColor("#CCD6F6"))
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(64, 64, 64, 64)
            text = "Winifred is not answering.\n\nCheck the connection, then tap here to try again."
            visibility = View.GONE
            setBackgroundColor(Color.parseColor(NAVY))
            setOnClickListener { web.reload() }
        }

        val root = FrameLayout(this).apply {
            addView(
                web,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
            addView(
                trouble,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        setContentView(root)

        if (savedInstanceState != null) {
            /* Coming back from a rotation or a trip to the browser: keep
               the conversation rather than reloading the page. */
            web.restoreState(savedInstanceState)
        } else {
            web.loadUrl(START_URL)
        }
    }

    private fun openOutside(uri: Uri) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (_: Exception) {
            /* No browser installed. Nothing to do, and nothing worth
               interrupting her with. */
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) {
            web.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::web.isInitialized) web.saveState(outState)
    }

    override fun onPause() {
        super.onPause()
        if (::web.isInitialized) {
            web.onPause()
            /* Pages she is not looking at must not keep a timer running. */
            web.pauseTimers()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::web.isInitialized) {
            web.resumeTimers()
            web.onResume()
        }
    }

    override fun onDestroy() {
        if (::web.isInitialized) {
            web.loadUrl("about:blank")
            web.destroy()
        }
        super.onDestroy()
    }
}
