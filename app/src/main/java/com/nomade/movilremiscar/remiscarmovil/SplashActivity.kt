package com.nomade.movilremiscar.remiscarmovil

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.nomade.movilremiscar.remiscarmovil.databinding.ActivityMainBinding
import com.nomade.movilremiscar.remiscarmovil.databinding.ActivitySplashBinding
import com.nomade.movilremiscar.remiscarmovil.utils.Constants
import com.nomade.movilremiscar.remiscarmovil.utils.SharedPrefsUtil

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    var versionName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
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

        getVersionName()
        binding.textVersion.text = "VERSION ${versionName}"

        delayAndStart()
    }

    private fun delayAndStart() {
        Handler().postDelayed({
            val startMain = Intent(this, MainActivity::class.java)
            startActivity(startMain)
            finish()
        }, 5000)

    }

    private fun getVersionName() {
        try {
            val version = SharedPrefsUtil.get(Constants.VERSION_NAME_KEY, "")
            if (version.isNullOrEmpty()) {
                versionName = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), 0).versionName.toString()
                SharedPrefsUtil.set(Constants.VERSION_NAME_KEY, versionName)
            } else {
                versionName = version
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }
}