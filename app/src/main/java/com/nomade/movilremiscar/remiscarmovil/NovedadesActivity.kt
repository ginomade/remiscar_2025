package com.nomade.movilremiscar.remiscarmovil

import android.content.Intent
import android.graphics.Color
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.nomade.movilremiscar.remiscarmovil.databinding.ActivityNovedadesBinding
import com.nomade.movilremiscar.remiscarmovil.utils.Constants
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.USER_EMAIL_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.USER_MOVIL_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.SharedPrefsUtil


class NovedadesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNovedadesBinding
    val TAG = "NovedadesActivity"
    var userEmail = ""
    var movil = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNovedadesBinding.inflate(layoutInflater)
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

        userEmail = SharedPrefsUtil.get(USER_EMAIL_KEY, "")
        movil = SharedPrefsUtil.get(USER_MOVIL_KEY, "")

        binding.buttonVenc.setOnClickListener {
            val finalUrl = Constants.VENCIMIENTOS_ADD + "?IMEI=" + userEmail + "&Movil=" + movil
            binding.webView.loadUrl(finalUrl)
            binding.buttonCamUsu.visibility = GONE
            binding.buttonVenc.visibility = GONE
        }

        binding.buttonCamUsu.setOnClickListener {
            val intent = Intent(this@NovedadesActivity, CamUsuActivity::class.java)
            startActivity(intent)
        }

        binding.buttonInicio.setOnClickListener {
            finish()
        }


    }

    fun setWebview() {

        val finalUrl = Constants.NOVEDADES_ADD + "?IMEI=" + userEmail + "&Movil=" + movil
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

    override fun onResume() {
        super.onResume()
        setWebview()
    }

}