package com.nomade.movilremiscar.remiscarmovil

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

        checkAndRequestPermissions(this)

        //delayAndStart()
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

    //permisos de camara y archivos

    // Llama a esta función cuando los permisos están listos
    private fun iniciarCamara() {
        // Aquí es donde ejecutas el código para iniciar la app
        Log.i("App", "Permisos listos, abriendo la cámara...")
        delayAndStart()
    }

    // Llama a esta función cuando los permisos son denegados
    private fun mostrarMensajePermisosDenegados() {
        // Informa al usuario que la funcionalidad de la cámara no estará disponible.
        // Puedes usar un Snackbar o un Dialog.
        // Opcionalmente, puedes usar shouldShowRequestPermissionRationale() para explicar
        // por qué los permisos son necesarios si el usuario los denegó por primera vez.
        Log.w("App", "Funcionalidad de cámara deshabilitada por falta de permisos.")
    }

    private val PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA
    )

    // 1. Declarar el ActivityResultLauncher para manejar la respuesta
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            // El mapa 'permissions' contiene la clave (nombre del permiso) y un booleano (true = concedido, false = denegado)

            // Verifica si todos los permisos requeridos fueron concedidos
            val allGranted = permissions.entries.all { it.value == true }

            if (allGranted) {
                // ✅ Todos los permisos fueron concedidos.
                Log.d("Permisos", "Todos los permisos concedidos. Iniciar función de cámara.")
                iniciarCamara()
            } else {
                // ❌ Al menos un permiso fue denegado.
                Log.e("Permisos", "Al menos un permiso fue denegado.")
                mostrarMensajePermisosDenegados()
            }
        }

    private fun checkAndRequestPermissions(context: Context) {

        // Filtra para obtener solo los permisos que *no* han sido concedidos aún.
        val permissionsToRequest = PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isEmpty()) {
            // Los permisos ya están concedidos.
            iniciarCamara()
        } else {
            // Solicita los permisos faltantes.
            requestPermissionsLauncher.launch(permissionsToRequest)
            //
        }
    }
}