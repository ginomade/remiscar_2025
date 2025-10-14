package com.nomade.movilremiscar.remiscarmovil

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.location.GpsStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.nomade.movilremiscar.remiscarmovil.databinding.ActivityMapsBinding
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.COORDENADAS_VIAJE_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.SharedPrefsUtil
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MapsActivity : AppCompatActivity(), MapListener, GpsStatus.Listener, LocationListener {
    private lateinit var binding: ActivityMapsBinding

    val TAG = "MapsActivity"

    lateinit var mMap: MapView
    lateinit var controller: IMapController
    lateinit var mMyLocationOverlay: MyLocationNewOverlay

    private var geopos = ""
    val track = ArrayList<GeoPoint>()


    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
	
	var coordenadasViaje = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
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

        Configuration.getInstance().setUserAgentValue(this.packageName)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

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

        Log.e(TAG, "onCreate:in ${controller.zoomIn()}")
        Log.e(TAG, "onCreate: out  ${controller.zoomOut()}")


        mMap.overlays.add(mMyLocationOverlay)

        


        mMap.addMapListener(this)

        binding.buttonIr.setOnClickListener {
            if (coordenadasViaje != "") {
                SharedPrefsUtil.set(COORDENADAS_VIAJE_KEY, "")
                startDirections(coordenadasViaje)
            } else {
                Toast.makeText(this, "No se encontro la dirección.", Toast.LENGTH_SHORT).show()
            }
            }

    }

    fun addMapMarker(lat: String, lon: String) {
        val startPoint = GeoPoint(lat.toDouble(), lon.toDouble())
        val startMarker = Marker(mMap)
        startMarker.icon =
            getDrawable(com.google.android.material.R.drawable.ic_arrow_back_black_24)
        startMarker.position = startPoint
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        mMap.overlays.add(startMarker)
        mMap.invalidate()
        track.add(startPoint)
        if (track.size > 1) {
            //addtrace()
        }
    }

    fun addtrace() {
        val roadManager: RoadManager = OSRMRoadManager(this, this.packageName)
        val road: Road = roadManager.getRoad(track)
        val roadOverlay: Polyline = RoadManager.buildRoadOverlay(road)
        mMap.getOverlays().add(roadOverlay)
        mMap.invalidate()
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        /*Log.e(TAG, "onCreate:la ${event?.source?.getMapCenter()?.latitude}")
        Log.e(TAG, "onCreate:lo ${event?.source?.getMapCenter()?.longitude}")*/
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        Log.e(TAG, "onZoom zoom level: ${event?.zoomLevel}   source:  ${event?.source}")
        return false
    }

    override fun onGpsStatusChanged(event: Int) {

    }

    override fun onResume() {
        super.onResume()
		
		coordenadasViaje = SharedPrefsUtil.get(COORDENADAS_VIAJE_KEY, "")
        if (!coordenadasViaje.isNullOrEmpty()) {
            val latViaje = coordenadasViaje.split(",").first()
            val lonViaje = coordenadasViaje.split(",").last()
            addMapMarker(latViaje, lonViaje)
        }
		
        getLocation()
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
        if (mMap != null) {
            addMapMarker(location.latitude.toString(), location.longitude.toString())
        }
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

    fun startDirections(geopos: String) {

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${geopos}")
        )
        startActivity(intent)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.w(TAG, "localizacion onStatusChanged")
    }
}