package com.nomade.movilremiscar.remiscarmovil

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.nomade.movilremiscar.remiscarmovil.databinding.ActivityCronoBinding

class CronoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCronoBinding
    val TAG = "CronoActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityCronoBinding.inflate(layoutInflater)
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

        binding.buttonInicio.setOnClickListener {
            finish()
        }

        binding.buttonContar.setOnClickListener {
            binding.buttonContar.isEnabled = false
            binding.buttonInicio.isEnabled = false
            binding.buttonPausa.isEnabled = true
            binding.chronometer.base = SystemClock.elapsedRealtime()
            binding.buttonPausa.setTextColor(Color.parseColor("#FA67FF01"))
            binding.buttonInicio.setTextColor(Color.parseColor("#ffff1b00"))
            binding.chronometer.start()
        }
        binding.buttonPausa.setOnClickListener {
            binding.buttonContar.isEnabled = true
            binding.buttonInicio.isEnabled = true
            binding.buttonPausa.isEnabled = false
            binding.buttonPausa.setTextColor(Color.parseColor("#FFFFFFFF"))
            binding.buttonInicio.setTextColor(Color.parseColor("#FFFFFFFF"))
            binding.chronometer.stop()
        }
    }

}