package com.nomade.movilremiscar.remiscarmovil

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.nomade.movilremiscar.remiscarmovil.databinding.ActivityPanicBinding
import com.nomade.movilremiscar.remiscarmovil.utils.Constants
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.ALERTA
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.PRUEBA
import com.nomade.movilremiscar.remiscarmovil.utils.SharedPrefsUtil
import java.net.URLEncoder

class PanicActivity : AppCompatActivity(), LocationListener {
    private lateinit var viewModel: AlertaViewModel
    private lateinit var binding: ActivityPanicBinding
    val TAG = "PanicActivity"
    var userEmail = ""
    var movil = ""
    var geopos = ""
    var latMovil = ""
    var lonMovil = ""
    var tipoAlerta = ""
    var direccion = ""

    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPanicBinding.inflate(layoutInflater)
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

        viewModel = ViewModelProvider(this).get(AlertaViewModel::class.java)

        userEmail = SharedPrefsUtil.get(Constants.USER_EMAIL_KEY, "")
        movil = SharedPrefsUtil.get(Constants.USER_MOVIL_KEY, "")

        binding.buttonPrueba.setOnClickListener {
            if (geopos.isEmpty()) {
                Toast.makeText(
                    this,
                    "Localizacion no detectada. Vuelva a intentar.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                binding.buttonPrueba.setBackgroundColor(Color.parseColor("#326166"))
                tipoAlerta = PRUEBA
                iniciarAlerta()
            }
        }

        binding.buttonAlerta.setOnClickListener {
            if (geopos.isEmpty()) {
                Toast.makeText(
                    this,
                    "Localizacion no detectada. Vuelva a intentar.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                tipoAlerta = ALERTA
                iniciarAlerta()
            }
        }

        binding.buttonInicio.setOnClickListener {
            finish()
        }

        observeViewModel()
        getLocation()

        binding.textConfirmacion.visibility = View.GONE
    }

    private fun observeViewModel() {

        viewModel.getDir().observe(this) { dir ->
            if (dir.isNotEmpty()) {
                direccion = URLEncoder.encode(dir, "utf-8")
                Log.d(TAG, "enviando alerta direccion - ${direccion}")
                viewModel.enviarAlerta(tipoAlerta, movil, userEmail, direccion, geopos)
            }
        }

        viewModel.getState().observe(this) { state ->
            if (state) {
                //Toast.makeText(this, "PRUEBA de alerta enviada.", Toast.LENGTH_LONG).show()
                binding.textConfirmacion.visibility = View.VISIBLE
            }
        }

        viewModel.getAlertState().observe(this) { state ->
            if (state) {
                finish()
            }
        }
    }


    private fun iniciarAlerta() {
        if (geopos.isNotEmpty())
            viewModel.buscarDireccion(geopos)
    }

    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }

    override fun onLocationChanged(location: Location) {
        geopos = "${location.latitude},${location.longitude}"
        Log.w(TAG, "update localizacion - ${geopos}")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.w(TAG, "localizacion onStatusChanged")
    }
}