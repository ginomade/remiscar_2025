package com.nomade.movilremiscar.remiscarmovil

import android.Manifest
import android.R
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.common.AccountPicker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.nomade.movilremiscar.remiscarmovil.databinding.ActivityMainBinding
import com.nomade.movilremiscar.remiscarmovil.utils.Constants
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.ALERTA
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.AL_FECHA_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.AL_GEOPOS_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.AL_MOVIL_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.AL_STATUS_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.AL_UBICACION_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.INICIO_ADD
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.MAIN_VIEW_ADD
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.SEGUIMIENTO
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.SESION_INICIADA
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.USER_EMAIL_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.USER_LOCATION_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.USER_MOVIL_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.VERSION_NAME_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.SharedPrefsUtil
import com.nomade.movilremiscar.remiscarmovil.utils.UserDialogFragment
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    private val EMAIL_REQUEST_CODE = 1
    private val TAG_MAIN = "MainActivity"

    private var enableStart = false
    private var userEmail = ""
    private var geopos = "" // lat,lon
    private var movil = ""
    private var flg_mens = false
    private var flg_autodespacho = false
    private var flg_puntero_alt = false
    private var flg_puntero_libre = false
    private var flg_puntero_viaje = false
    private var flg_puntero_parada = false
    private var movil_en_alerta = false
    private var direccion = ""
    private var flg_webview_started = false
    private var flg_inicio = false


    private var map: GoogleMap? = null

    // The entry point to the Places API.
    private lateinit var placesClient: PlacesClient

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var locationPermissionGranted = false

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null

    private val requestingLocationUpdates = true

    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private val DEFAULT_ZOOM = 15
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

    var versionName = ""

    var active = false

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
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
        window.addFlags(FLAG_KEEP_SCREEN_ON)

        userDisclosure()

        SharedPrefsUtil.init(this)

        getVersionName()

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding.buttonReload.setOnClickListener {
            setWebview()
        }

        val popupMenu = PopupMenu(this, binding.menuIcon)
        popupMenu.menu.add(1, 1, 1, "Reclamos")
        popupMenu.menu.add(1, 2, 2, "Políticas de Privacidad")
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    val intentReclamos = Intent(this@MainActivity, ReclamosActivity::class.java)
                    intentReclamos.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    startActivity(intentReclamos)
                }

                2 -> {
                    val intentPrivacy = Intent(this@MainActivity, PrivacyActivity::class.java)
                    intentPrivacy.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    startActivity(intentPrivacy)
                }
            }
            true
        })
        binding.menuIcon.setOnClickListener {
            popupMenu.show()
        }

        binding.buttonMensajes.setOnClickListener {
            val intent = Intent(this@MainActivity, NovedadesActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        }

        binding.buttonCrono.setOnClickListener {
            val intent = Intent(this@MainActivity, CronoActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        }

        binding.buttonAlerta.setOnClickListener {
            val intent = Intent(this@MainActivity, PanicActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        }

        binding.buttonMapa.setOnClickListener {
            val intent = Intent(this@MainActivity, MapsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        }

        binding.frmAlerta.setOnClickListener {
            val intent = Intent(this@MainActivity, AlertaActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        }

        geopos = ""
        SharedPrefsUtil.set(USER_LOCATION_KEY, geopos)
        getInitialLocation()

        Log.d(
            "SESION_INICIADA mainactivity oncreate",
            SharedPrefsUtil.get(SESION_INICIADA, false).toString()
        )
        if (SharedPrefsUtil.get(SESION_INICIADA, false)) {
            startServices()
        }
    }

    private fun startServices() {
        if (hasLocation(this)) {
            startLocationService()
        }

        val syncRequest = OneTimeWorkRequestBuilder<LocationWorker>()
            .setInitialDelay(40, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this).enqueue(syncRequest)
    }

    fun startLocationService() {
        if (LocationService.isServiceRunning) {
            Log.d("ServiceStarter", "LocationService ya está en ejecución. No se hace nada.")
            return
        }
        val serviceIntent = Intent(this, LocationService::class.java)
        startForegroundService(serviceIntent)
    }

    private fun hasLocation(ctx: Context) =
        ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PERMISSION_GRANTED

    fun getInitialLocation() {
        // [START_EXCLUDE silent]
        // Construct a PlacesClient
        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(this)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Build the map.
        // [START maps_current_place_map_fragment]
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager
            .beginTransaction()
            .add(binding.mapContainer.id, mapFragment)
            .commit()

        mapFragment.getMapAsync(this)
        binding.mapContainer.visibility = GONE
        // [END maps_current_place_map_fragment]
        // [END_EXCLUDE]

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations) {
                    val lat = location.latitude
                    val lon = location.longitude
                    Log.w(
                        TAG_MAIN,
                        "getCurrentLocation localizacion $lat,$lon"
                    )
                    geopos = "$lat,$lon"
                    updateLocationInMap(location)
                }
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(15)
            fastestInterval = TimeUnit.SECONDS.toMillis(5)
            maxWaitTime = TimeUnit.MINUTES.toMillis(2)
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            })
            .addOnSuccessListener { location: Location? ->
                if (location == null)
                    Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
                else {
                    Log.w(
                        TAG_MAIN,
                        "getCurrentLocation localizacion " + location!!.latitude + "," + location!!.longitude
                    )
                    val lat = location.latitude
                    val lon = location.longitude
                    geopos = "$lat,$lon"
                    //Log.w("TEST LOC", geopos)
                    updateLocationInMap(location)
                }

            }

    }

    private fun updateLocationInMap(location: Location) {
        map?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    location.latitude,
                    location.longitude
                ), 16f
            )
        )
        val marcador = LatLng(location.latitude, location.longitude)

        map?.addMarker(
            MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow_down_float))
                .position(marcador)
        )
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    // [START maps_current_place_on_map_ready]
    override fun onMapReady(map: GoogleMap) {
        this.map = map
        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Prompt the user for permission.
        getLocationPermission()
        // [END_EXCLUDE]

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()
    }
    // [END maps_current_place_on_map_ready]

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    // [START maps_current_place_get_device_location]
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.

                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {

                            val lat = lastKnownLocation!!.latitude
                            val lon = lastKnownLocation!!.longitude
                            Log.w(
                                TAG_MAIN,
                                "update localizacion $lat,$lon"
                                //aca
                            )
                            geopos = "$lat,$lon"
                            SharedPrefsUtil.set(USER_LOCATION_KEY, geopos)
                            Log.w("TEST LOC", geopos)
                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lat,
                                        lon
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    } else {
                        Log.d(TAG_MAIN, "Current location is null. Using defaults.")
                        Log.e(TAG_MAIN, "Exception: %s", task.exception)
                        map?.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
                startLocationService()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    // [END maps_current_place_get_device_location]

    // [START maps_current_place_update_location_ui]
    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    // [END maps_current_place_update_location_ui]

    private fun getGeopos(): String {
        var ret = ""
        if (!geopos.isNullOrEmpty()) {
            ret = geopos.toString()
        }
        return ret
    }

    private fun getVersionName() {
        try {
            val version = SharedPrefsUtil.get(Constants.VERSION_NAME_KEY, "")
            if (version.isNullOrEmpty()) {
                versionName = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), 0).versionName.toString()
                SharedPrefsUtil.set(VERSION_NAME_KEY, versionName)
            } else {
                versionName = version

            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    fun checkAndInitialize() {
        enableStart = true
        geopos = SharedPrefsUtil.get(USER_LOCATION_KEY, "")
        // check permisos

        // check condiciones
        if (!SharedPrefsUtil.get(Constants.PRIVACY_KEY, false)) {
            enableStart = false
            validarPrivacyOIniciar()
        } else {

            // check email user
            userEmail = SharedPrefsUtil.get(USER_EMAIL_KEY, "")

            validarUsuario()

            // start
            if (enableStart) {
                observeViewModel()
                setWebview()
                iniciarTimer()
            }

        }
    }

    private fun iniciarTimer() {

        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread { tareasPeriodicas() }
            }
        }, 8000)
    }

    private fun validarUsuario() {
        if (userEmail.isEmpty()) {
            enableStart = false
            getUserAccount()
        } else {
            viewModel.validarUsuario(userEmail)
        }
    }

    private fun userDisclosure() {
        if (!SharedPrefsUtil.get(Constants.USER_DIALOG_KEY, false)) {
            val dialog = UserDialogFragment()
            dialog.setKeyType(Constants.USER_DIALOG_KEY)
            dialog.show(supportFragmentManager, "USER_DIALOG")
        }
    }

    fun getUserAccount() {
        val intent = AccountPicker.newChooseAccountIntent(
            AccountPicker.AccountChooserOptions.Builder()
                .setAllowableAccountsTypes(listOf("com.google"))
                .build()
        )

        startActivityForResult(intent, EMAIL_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === EMAIL_REQUEST_CODE && resultCode === RESULT_OK) {
            val accountName: String? = data!!.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!accountName.isNullOrEmpty()) {
                SharedPrefsUtil.set(USER_EMAIL_KEY, accountName)
                userEmail = accountName
                checkAndInitialize()
            } else {
                Log.w(TAG_MAIN, "Error al obtener email.")
            }
        }
    }

    private fun observeViewModel() {

        viewModel.getUserInfo().observe(this) { info ->
            if (!info.isNullOrEmpty()) {
                Log.d(TAG_MAIN, info)
                movil = info
                binding.textMovil.text = movil
                SharedPrefsUtil.set(USER_MOVIL_KEY, movil)
            }
        }

        viewModel.getUserStatus().observe(this) { state ->
            when (state) {
                "1" -> enableUser()
                "2" -> enablePropietario()
                "0" -> noAutorizado()
            }
        }

        viewModel.getResultMensaje().observe(this) { result ->
            when (result) {
                "1" -> {
                    if (!flg_mens) {
                        flg_mens = true
                        binding.buttonMensajes.text = "HAY MENSAJES"
                        binding.buttonMensajes.setTextColor(Color.parseColor("#d5d9ea"))
                        binding.buttonMensajes.setBackgroundColor(Color.parseColor("#4665A3"))
                        Toast.makeText(this, "Hay nuevos mensajes para usted.", Toast.LENGTH_LONG)
                            .show();
                        alarmaSonora()
                    }
                }

                "0" -> {
                    binding.buttonMensajes.text = "NOVEDADES"
                    binding.buttonMensajes.setTextColor(Color.parseColor("#FFFFFFFF"))
                    binding.buttonMensajes.setBackgroundColor(Color.parseColor("#FF0000ff"))
                    flg_mens = false
                }
            }
        }

        viewModel.getResultAlerta().observe(this) { result ->
            run {
                val status = result.status
                val al_fecha = result.Fecha
                val al_geopos = result.GeoPos
                val al_movil = result.Movil
                val al_ubicacion = result.Ubicacion
                if (status == ALERTA || status == SEGUIMIENTO) {
                    if (movil == al_movil) {
                        // la alerta es de este movil, envio actualizacion de datos.
                        viewModel.buscarDireccion(getGeopos())
                        movil_en_alerta = true
                    } else {
                        binding.frmAlerta.visibility = View.VISIBLE
                        movil_en_alerta = false
                    }

                    SharedPrefsUtil.set(AL_STATUS_KEY, status)
                    SharedPrefsUtil.set(AL_GEOPOS_KEY, al_geopos)
                    SharedPrefsUtil.set(AL_MOVIL_KEY, al_movil)
                    SharedPrefsUtil.set(AL_FECHA_KEY, al_fecha)
                    SharedPrefsUtil.set(AL_UBICACION_KEY, al_ubicacion)

                } else {
                    binding.frmAlerta.visibility = View.GONE
                    movil_en_alerta = false
                }
            }

        }

        viewModel.getDir().observe(this) { result ->
            run {
                direccion = result
                if (movil_en_alerta) {
                    // este movil esta en alerta, envio actualisacion de ubicacion
                    viewModel.enviarAlerta(SEGUIMIENTO, movil, userEmail, direccion, getGeopos())

                }
            }
        }

        viewModel.getResultAuto().observe(this) { estadoMovil ->
            run {
                var result = estadoMovil.result
                estadoAutodespacho(result.toString())
            }
        }

        viewModel.getResultMovilPuntero().observe(this) { estadoMovil ->
            run {
                var result = estadoMovil.result
                estadoMovil(result.toString())
            }
        }

        viewModel.getResultPunteroAlt().observe(this) { estadoMovil ->
            run {
                var result = estadoMovil.result
                estadoPunteroAlternativo(result.toString())
            }
        }
        viewModel.getResultPunteroLibre().observe(this) { estadoMovil ->
            run {
                var result = estadoMovil.result
                estadoPunteroLibre(result.toString())
            }
        }
        viewModel.getResultPunteroViaje().observe(this) { estadoMovil ->
            run {
                var result = estadoMovil.result
                estadoPunteroViaje(result.toString())
            }
        }
        viewModel.getResultPunteroSalioParada().observe(this) { estadoMovil ->
            run {
                var result = estadoMovil.result
                estadoPunteroSalioParada(result.toString())
            }
        }
    }

    fun estadoAutodespacho(estado: String) {
        when (estado) {
            "1" -> {
                if (!flg_autodespacho) {
                    flg_autodespacho = true

                    Toast.makeText(this, "Autodespacho", Toast.LENGTH_LONG).show()
                    alarmaSonora2()
                }
            }

            else -> {
                flg_autodespacho = false
            }
        }
    }

    fun estadoPunteroAlternativo(estado: String) {
        when (estado) {
            "1" -> {
                if (!flg_puntero_alt) {
                    flg_puntero_alt = true

                    Toast.makeText(this, "PunteroAlternativa", Toast.LENGTH_LONG).show()
                    alarmaSonora2()
                }
            }

            else -> {
                flg_puntero_alt = false
            }
        }
    }

    fun estadoPunteroLibre(estado: String) {
        when (estado) {
            "1" -> {
                if (!flg_puntero_libre) {
                    flg_puntero_libre = true

                    Toast.makeText(this, "PunteroLibre", Toast.LENGTH_LONG).show()
                    alarmaSonora2()
                }
            }

            else -> {
                flg_puntero_libre = false
            }
        }
    }

    fun estadoPunteroViaje(estado: String) {
        when (estado) {
            "1" -> {
                if (!flg_puntero_viaje) {
                    flg_puntero_viaje = true

                    Toast.makeText(this, "PunteroViaje", Toast.LENGTH_LONG).show()
                    alarmaSonora2()
                }
            }

            else -> {
                flg_puntero_viaje = false
            }
        }
    }

    fun estadoPunteroSalioParada(estado: String) {
        when (estado) {
            "1" -> {
                if (!flg_puntero_parada) {
                    flg_puntero_parada = true

                    Toast.makeText(this, "PunteroSalioParada", Toast.LENGTH_LONG).show()
                    alarmaSonora2()
                }
            }

            else -> {
                flg_puntero_parada = false
            }
        }
    }

    fun estadoMovil(estado: String) {
        when (estado) {
            "1" -> {
                if (!flg_puntero_parada) {
                    flg_puntero_parada = true

                    Toast.makeText(this, "estadoMovil", Toast.LENGTH_LONG).show()
                    alarmaSonora2()
                }
            }

            else -> {
                flg_puntero_parada = false
            }
        }
    }

    fun alarmaSonora() {
        val resId = resources.getIdentifier("c2answer", "raw", packageName)
        mediaPlayer = MediaPlayer.create(this, resId)
        mediaPlayer?.start()
    }

    fun alarmaSonora2() {
        val resId = resources.getIdentifier("c2answerv", "raw", packageName)
        mediaPlayer = MediaPlayer.create(this, resId)
        mediaPlayer?.start()

    }

    private fun tareasPeriodicas() {
        // tareas ejecutadas periodicamente desde viewmodel

        if (!flg_webview_started) {

            setWebview()
        }

        binding.textMovil.text = movil
        if (movil.isNotEmpty() && userEmail.isNotEmpty()) {
            viewModel.buscarMensajes(userEmail, movil)
            Thread.sleep(150)
            Log.d(
                "SESION_INICIADA mainactivity tareasPeriodicas",
                SharedPrefsUtil.get(SESION_INICIADA, false).toString()
            )
            if (SharedPrefsUtil.get(SESION_INICIADA, false)) {
                requestList()
                Thread.sleep(150)
                viewModel.buscarCoordenadasViaje(userEmail, movil)
            }
        }

        if (active) {
            iniciarTimer()
        }
    }

    private fun requestList() {
        if (geoposValid()) {
            val mensaje = "${userEmail} , movil: ${movil}, geo: ${geopos}"
            //Bugfender.d("Remis", mensaje)

            viewModel.buscarAlerta(userEmail, movil, "direccion", getGeopos())
            Thread.sleep(150)
            viewModel.buscarAuto(userEmail, movil, getGeopos())
            Thread.sleep(150)
            viewModel.buscarMovilPuntero(userEmail, movil, getGeopos())
            Thread.sleep(150)
            viewModel.buscarPunteroLibre(userEmail, movil, getGeopos())
            Thread.sleep(150)
            viewModel.buscarPunteroAlternativa(userEmail, movil, getGeopos())
            Thread.sleep(150)
            viewModel.buscarPunteroViaje(userEmail, movil, getGeopos())
            Thread.sleep(150)
            viewModel.buscarPunteroSalioParada(userEmail, movil, getGeopos())
            Thread.sleep(150)
            viewModel.enviarGeopos(userEmail, movil, getGeopos())
            Thread.sleep(150)
            Log.w("TEST LOC", getGeopos())
            viewModel.enviarGeoposMviajeshoy(userEmail, movil, getGeopos())

            Log.d(
                "SESION_INICIADA mainactivity requestList",
                SharedPrefsUtil.get(SESION_INICIADA, false).toString()
            )
            if (!SharedPrefsUtil.get(SESION_INICIADA, false)) {
                stopService(this, LocationService::class.java)
            }
        }
    }

    private fun noAutorizado() {
        Toast.makeText(this, "App solo para propietarios autorizados.", Toast.LENGTH_LONG)
            .show()
        finish()
    }

    private fun geoposValid(): Boolean {
        return !(getGeopos().isNullOrEmpty() || getGeopos() == "")
    }

    private fun enablePropietario() {
        val intent = Intent(this@MainActivity, PropActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun enableUser() {
    }

    fun stopService(
        context: Context,
        serviceClass: Class<*>
    ) {

        // 🔹 Detener el Service si está en ejecución
        val serviceIntent = Intent(context, serviceClass)
        context.stopService(serviceIntent)
    }

    // valido el momento en que la webview pasa de Mviajeshoyinicio a Mviajeshoy para actualizar SESION_INICIADA
    fun checkInicioSesion(url: String) {
        var inicio_url = !url.toString().lowercase().contains("inicio")

        if (!flg_inicio && inicio_url) {
            viewModel.enviarGeopos(userEmail, movil, getGeopos())
            SharedPrefsUtil.set(
                SESION_INICIADA,
                true
            )
            startServices()
        }
        flg_inicio = inicio_url
    }

    fun setWebview() {
        var webViewUrl = ""
        Log.d("SESION_INICIADA webview", SharedPrefsUtil.get(SESION_INICIADA, false).toString())
        if (SharedPrefsUtil.get(SESION_INICIADA, false)) {
            webViewUrl = MAIN_VIEW_ADD
        } else {
            webViewUrl = INICIO_ADD
        }

        val finalUrl =
            "${webViewUrl}?imei=${userEmail}&Movil=${movil}&geopos=${getGeopos()}"
        Log.d(TAG_MAIN, finalUrl)
        binding.mainWebview.settings.setJavaScriptEnabled(true)

        binding.mainWebview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                flg_webview_started = !url.toString().lowercase().contains("mviajeshoy")
                checkInicioSesion(url!!)
                return true
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                binding.mainWebview.loadData(
                    "<html><body><h1>Cargando...</h1></body></html>",
                    "text/html",
                    null
                )
                Thread.sleep(200)
                binding.mainWebview.loadUrl(finalUrl)
            }
        }
        flg_webview_started = false
        binding.mainWebview.loadUrl(finalUrl)
    }

    override fun onResume() {
        super.onResume()
        setWebview()
        checkAndInitialize()
        flg_webview_started = false
        validarGps()
        active = true
        if (requestingLocationUpdates) startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.release()
        mediaPlayer = null
        active = false
        //geopos = ""
        stopLocationUpdates()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (flg_webview_started) {
            setWebview()
        } else {
            super.onBackPressed()
        }
    }

    private fun validarPrivacyOIniciar() {
        val privacyIntent = Intent(this, PrivacyActivity::class.java)
        privacyIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(privacyIntent)
    }


    // [START maps_current_place_location_permission]
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }
    // [END maps_current_place_location_permission]

    /**
     * Handles the result of the request for location permissions.
     */
    // [START maps_current_place_on_request_permissions_result]
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        updateLocationUI()
    }
    // [END maps_current_place_on_request_permissions_result]

    fun validarGps() {
        var mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Checking GPS is enabled
        val mGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val mAlternative = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (mGPS) {
            Log.w(TAG_MAIN, "update localizacion GPS habilitada")

        } else if (mAlternative) {
            Toast.makeText(this, "Debe activar la localizacion.", Toast.LENGTH_SHORT).show()
        } else {
            Log.w(TAG_MAIN, "update localizacion GPS- ${mGPS}")
            Toast.makeText(this, "Debe activar la localizacion.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

}