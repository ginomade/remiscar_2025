package com.nomade.movilremiscar.remiscarmovil

import android.graphics.Color
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.nomade.movilremiscar.remiscarmovil.databinding.ActivityCamUsuBinding
import com.nomade.movilremiscar.remiscarmovil.utils.Constants
import com.nomade.movilremiscar.remiscarmovil.utils.SharedPrefsUtil

class CamUsuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCamUsuBinding
    val TAG = "CamUsuActivity"
    var userEmail = ""
    var movil = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCamUsuBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                bars.top,          // evita superposición con status bar
                v.paddingRight,
                bars.bottom        // evita superposición con nav bar / gestos
            )
            insets
        }

        userEmail = SharedPrefsUtil.get(Constants.USER_EMAIL_KEY, "")
        movil = SharedPrefsUtil.get(Constants.USER_MOVIL_KEY, "")

        binding.buttonCamUsu.setOnClickListener {
            val finalUrl = Constants.CAMUSU_ADD + "?IMEI=" + userEmail + "&Movil=" + movil
            binding.webView.loadUrl(finalUrl)
            binding.buttonCamUsu.visibility = View.GONE
        }

        binding.buttonInicio.setOnClickListener {
            finish()
        }

        setWebview()
    }

    fun setWebview() {

        val finalUrl = Constants.CAMBIO_ADD + "?IMEI=" + userEmail + "&Movil=" + movil
        Log.d(TAG, finalUrl)
        binding.webView.settings.setJavaScriptEnabled(true)

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
            }



            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                binding.webView.loadData(
                    "<html><body><h1>Cargando...</h1></body></html>",
                    "text/html",
                    null
                );
            }
        }
        binding.webView.loadUrl(finalUrl)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}