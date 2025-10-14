package com.nomade.movilremiscar.remiscarmovil

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.nomade.movilremiscar.remiscarmovil.databinding.ActivityPropBinding
import com.nomade.movilremiscar.remiscarmovil.utils.Constants
import com.nomade.movilremiscar.remiscarmovil.utils.SharedPrefsUtil

class PropActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPropBinding
    val TAG = "PropActivity"
    var userEmail = ""
    var movil = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPropBinding.inflate(layoutInflater)
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

        setWebview()
    }

    fun setWebview() {

        val finalUrl = Constants.PROP_ADD + "?IMEI=" + userEmail
        Log.d(TAG, finalUrl)
        binding.webView.settings.setJavaScriptEnabled(true)

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
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