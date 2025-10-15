package com.nomade.movilremiscar.remiscarmovil

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.nomade.movilremiscar.remiscarmovil.net.RetrofitService
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.USER_EMAIL_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.USER_LOCATION_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.USER_MOVIL_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.SharedPrefsUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class LocationWorker (appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    private var TAG = "LocationWorker"

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Iniciando sincronización de datos...")
            val location: Location? = fusedLocationClient.lastLocation.await()

            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude

                Log.i(TAG, "Ubicación obtenida: Lat=$latitude, Lon=$longitude")
                logLocation(location)

                scheduleNextWork()
                Result.success()
            } else {
                Log.w(TAG, "La ubicación es nula. Puede que el GPS esté apagado o no haya ubicación reciente.")
                Result.retry()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permiso de ubicación denegado.", e)
            Result.failure()
        } catch (e: Exception) {
            Log.e(TAG, "Error desconocido al obtener ubicación.", e)
            Result.retry()
        }
    }

    private fun logLocation(location: Location) {
        val message =
            "Ubicación Recibida -> Lat: ${location.latitude}, Lon: ${location.longitude}, Precisión: ${location.accuracy}m"
        Log.i(TAG, message)
        val email = SharedPrefsUtil.get(USER_EMAIL_KEY, "")
        val movil = SharedPrefsUtil.get(USER_MOVIL_KEY, "")
        val geopos = "${location.latitude},${location.longitude}"
        SharedPrefsUtil.set(USER_LOCATION_KEY, geopos)
        enviarGeopos(email, movil, geopos)
    }

    fun enviarGeopos(userEmail: String, movil: String, geopos: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.w(
                    TAG,
                    "enviando - enviarGeopos - email: ${userEmail} , movil: ${movil}, geo: ${geopos}"
                )
                val call: Call<String?>? =
                    RetrofitService.RetrofitInstance.api.enviarGeopos(
                        movil,
                        userEmail,
                        geopos
                    )
                call!!.enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        if (response.isSuccessful()) {
                            Log.w(TAG, "enviarGeopos: ${response.code()}")

                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG,
                            "enviarGeopos: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.w(TAG, "enviarGeopos: ${e}")
            }
        }
    }

    private fun scheduleNextWork() {
        // Construimos el OneTimeWorkRequest para que se ejecute después de 60 segundos
        val nextWorkRequest = OneTimeWorkRequestBuilder<LocationWorker>()
            .setInitialDelay(60, TimeUnit.SECONDS) // Configura el delay de 60 segundos
            .build()

        // Encolamos el siguiente trabajo
        WorkManager.getInstance(applicationContext).enqueue(nextWorkRequest)
    }
}