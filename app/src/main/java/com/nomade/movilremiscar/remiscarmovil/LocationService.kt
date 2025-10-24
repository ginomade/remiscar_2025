package com.nomade.movilremiscar.remiscarmovil

// LocationService.kt
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "location_channel_id"
        private const val NOTIFICATION_ID = 12345
        private const val TAG = "LocationService"

        var isServiceRunning = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        createLocationCallback()
        isServiceRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = buildForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Inicia las actualizaciones de ubicación
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            Log.d(TAG, "Solicitando actualizaciones de ubicación.")
        } catch (e: SecurityException) {
            Log.e(TAG, "Permiso de ubicación denegado al iniciar servicio.", e)
            stopSelf()
        }

        // El servicio se reiniciará si el sistema lo mata (si es necesario)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        // Detiene las actualizaciones de ubicación cuando el servicio se detiene
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d(TAG, "Servicio de ubicación detenido y actualizaciones eliminadas.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 40000 // Intervalo deseado (ej: 60 segundos)
            fastestInterval = 30000 // El más rápido (ej: 30 segundos)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    // **AQUÍ ESTÁ LA UBICACIÓN VÁLIDA Y RECIENTE**
                    Log.i(
                        TAG,
                        "Ubicación activa recibida: Lat=${location.latitude}, Lon=${location.longitude}"
                    )

                }
            }
        }
    }

    // Lógica para crear el canal de notificación para Android 8.0 (Oreo) y superior
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Actualizaciones de Ubicación",
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
        }
    }

    // Construye la notificación visible requerida para un Foreground Service
    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Rastreo de Ubicación")
            .setContentText("Obteniendo tu ubicación en segundo plano...")
            .setSmallIcon(R.drawable.icon_agencia) // Asegúrate de tener este ícono
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}