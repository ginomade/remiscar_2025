package com.nomade.movilremiscar.remiscarmovil

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.nomade.movilremiscar.remiscarmovil.databinding.ActivityReclamosBinding
import com.nomade.movilremiscar.remiscarmovil.utils.Constants
import com.nomade.movilremiscar.remiscarmovil.utils.SharedPrefsUtil
import java.net.URLEncoder

class ReclamosActivity : AppCompatActivity() {
    private lateinit var viewModel: ReclamosViewModel
    private lateinit var binding: ActivityReclamosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReclamosBinding.inflate(layoutInflater)
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

        viewModel = ViewModelProvider(this).get(ReclamosViewModel::class.java)

        binding.buttonRet.setOnClickListener {
            finish()
        }

        val userEmail = SharedPrefsUtil.get(Constants.USER_EMAIL_KEY, "")
        val movil = SharedPrefsUtil.get(Constants.USER_MOVIL_KEY, "")

        binding.buttonSend.setOnClickListener {
            val texto = URLEncoder.encode(binding.editReclamo.text.toString(), "utf-8");
            if (texto.isNullOrEmpty()) {
                Toast.makeText(this, "Escriba un mensaje para enviar.", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.enviarReclamo(userEmail, texto, movil)
            }
        }

        mostrarForm()

        observeViewModel()
    }

    fun mostrarForm() {
        binding.confirmacion.visibility = View.GONE
        binding.mensaje.visibility = View.VISIBLE
    }

    fun confirmacion() {
        binding.confirmacion.visibility = View.VISIBLE
        binding.mensaje.visibility = View.GONE
    }

    private fun observeViewModel() {

        viewModel.getState().observe(this) { state ->
            if (state) {
                confirmacion()
            }
        }
    }
}