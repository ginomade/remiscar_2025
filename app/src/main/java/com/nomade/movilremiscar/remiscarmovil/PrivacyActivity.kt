package com.nomade.movilremiscar.remiscarmovil

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.nomade.movilremiscar.remiscarmovil.databinding.ActivityPrivacyBinding
import com.nomade.movilremiscar.remiscarmovil.utils.Constants
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.PRIVACY_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.SharedPrefsUtil


class PrivacyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
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

        SharedPrefsUtil.init(this)

        setWebview()

        binding.buttonAceptar.setOnClickListener {
            if(binding.checkPrivacy.isChecked){
                SharedPrefsUtil.set(PRIVACY_KEY, true)
                finish()
                startMain()
            } else {
                Toast.makeText(this, "Debe aceptar las condiciones para continuar.", Toast.LENGTH_SHORT).show();
            }
        }

        checkPrivacyAceptada()
    }

    private fun startMain() {
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
    }

    fun setWebview() {

        val finalUrl = Constants.PRIVACY_ADD

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
            }

        }
        binding.webView.loadUrl(finalUrl)
    }

    private fun checkPrivacyAceptada() {
        if (SharedPrefsUtil.get(PRIVACY_KEY, false)) {
            binding.buttonAceptar.setVisibility(View.GONE)
            binding.checkPrivacy.setVisibility(View.GONE)
        } else {
            binding.buttonAceptar.setVisibility(View.VISIBLE)
            binding.checkPrivacy.setVisibility(View.VISIBLE)
        }
    }
}