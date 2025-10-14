package com.nomade.movilremiscar.remiscarmovil

import android.graphics.Color
import android.graphics.Rect
import android.location.GpsStatus
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.nomade.movilremiscar.remiscarmovil.databinding.ActivityAlertaBinding
import com.nomade.movilremiscar.remiscarmovil.utils.Constants
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.ALERTA
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.SEGUIMIENTO
import com.nomade.movilremiscar.remiscarmovil.utils.SharedPrefsUtil
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Timer
import java.util.TimerTask


class AlertaActivity : AppCompatActivity(), MapListener, GpsStatus.Listener {
    private lateinit var viewModel: AlertaViewModel
    private lateinit var binding: ActivityAlertaBinding
    val TAG = "AlertaActivity"
    var userEmail = ""
    var movil = ""

    var al_status = ""
    var al_geopos = ""
    var al_movil = ""
    var al_fecha = ""
    var al_ubicacion = ""
    var lat = ""
    var lon = ""

    var active = false

    lateinit var mMap: MapView
    lateinit var controller: IMapController
    lateinit var mMyLocationOverlay: MyLocationNewOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertaBinding.inflate(layoutInflater)
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

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Configuration.getInstance().setUserAgentValue(this.packageName)

        viewModel = ViewModelProvider(this).get(AlertaViewModel::class.java)

        mMap = binding.osmmap
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.mapCenter
        mMap.setMultiTouchControls(true)
        mMap.getLocalVisibleRect(Rect())
        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mMap)
        controller = mMap.controller

        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = true
        mMyLocationOverlay.runOnFirstFix {
            runOnUiThread {
                controller.setCenter(mMyLocationOverlay.myLocation);
                controller.animateTo(mMyLocationOverlay.myLocation)
            }
        }
        controller.setZoom(16.0)
        mMap.overlays.add(mMyLocationOverlay)
        mMap.addMapListener(this)

        getData()

        binding.buttonInicio.setOnClickListener {
            finish()
        }

        viewModel.getSeguimiento().observe(this) { result ->
            al_status = result.status.toString()
            if (al_status == ALERTA || al_status == SEGUIMIENTO) {
                al_geopos = result.geopos.toString()
                al_movil = result.movil.toString()
                al_fecha = result.fecha.toString()
                al_ubicacion = result.ubicacion.toString()
            }
            if (al_status == ALERTA && al_movil == movil) {

            } else {
                showData()
            }

        }

        if (!lat.isNullOrEmpty()) {
            addMapMarker(lat, lon)
        }

        iniciarTimer()
    }

    private fun getData() {
        try {
            userEmail = SharedPrefsUtil.get(Constants.USER_EMAIL_KEY, "")
            movil = SharedPrefsUtil.get(Constants.USER_MOVIL_KEY, "")
            al_status = SharedPrefsUtil.get(Constants.AL_STATUS_KEY, "")
            if (al_status == "ALERTA") {
                Log.d(
                    "Remiscar AL",
                    "dataAlerta:$al_fecha-$al_movil-$al_ubicacion-$al_geopos"
                )
                al_geopos = SharedPrefsUtil.get(Constants.AL_GEOPOS_KEY, "")
                al_movil = SharedPrefsUtil.get(Constants.AL_MOVIL_KEY, "")
                al_fecha = SharedPrefsUtil.get(Constants.AL_FECHA_KEY, "")
                al_ubicacion = SharedPrefsUtil.get(Constants.AL_UBICACION_KEY, "")
                showData()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showData() {

        binding.textMovil.text = al_movil
        binding.textUbicacion.text = al_ubicacion
        binding.textFecha.text = al_fecha
        if (!al_geopos.isNullOrEmpty()) {
            val separated = al_geopos.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            lat = separated[0]
            lon = separated[1]
            addMapMarker(lat, lon)
        }
    }

    private fun iniciarTimer() {

        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread { tareasPeriodicas() }
            }
        }, 5000)
    }

    private fun tareasPeriodicas() {
        viewModel.buscarAlertaSeguimiento(al_status, movil, userEmail, al_movil)

        if (active) {
            iniciarTimer()
        }
    }

    override fun onResume() {
        super.onResume()
        active = true
    }

    override fun onPause() {
        super.onPause()
        active = false
    }

    fun addMapMarker(lat: String, lon: String) {
        val startPoint = GeoPoint(lat.toDouble(), lon.toDouble())
        val startMarker = Marker(mMap)

        startMarker.position = startPoint
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        mMap.overlays.add(startMarker)
        mMap.invalidate()
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        return false
    }

    override fun onGpsStatusChanged(p0: Int) {
        //
    }
}