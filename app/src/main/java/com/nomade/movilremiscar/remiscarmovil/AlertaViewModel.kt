package com.nomade.movilremiscar.remiscarmovil

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.nomade.movilremiscar.remiscarmovil.net.AlertResponse
import com.nomade.movilremiscar.remiscarmovil.net.LocationData
import com.nomade.movilremiscar.remiscarmovil.net.LocationRetrofitService
import com.nomade.movilremiscar.remiscarmovil.net.RetrofitService
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.POSITION_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.PRUEBA
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AlertaViewModel : ViewModel() {
    private val state = MutableLiveData<Boolean>()
    private val alertState = MutableLiveData<Boolean>()
    private val resultDireccion = MutableLiveData<String>()
    private val resultSeguimiento = MutableLiveData<AlertResponse>()

    var TAG = "AlertaViewModel"

    fun getState(): LiveData<Boolean> {
        return state
    }

    fun getAlertState(): LiveData<Boolean> {
        return alertState
    }

    fun getDir(): LiveData<String> {
        return resultDireccion
    }

    fun getSeguimiento(): LiveData<AlertResponse> {
        return resultSeguimiento
    }

    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.w(TAG, "Exception handled: ${throwable.localizedMessage}")
    }

    fun buscarDireccion(geopos: String) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                val call: Call<String?>? =
                    LocationRetrofitService.LocationRetrofitInstance.api.getAddress(
                        POSITION_KEY,
                        geopos
                    )
                call!!.enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        Log.w(TAG, "Response: ${response.isSuccessful}")
                        if (response.isSuccessful()) {
                            Log.w(TAG, "Response: ${response.body()}")
                            val jsonResponse = response.body().toString()
                            val gson = Gson()
                            var movilData = gson.fromJson(jsonResponse, LocationData::class.java)
                            Log.w(TAG, "Response location: ${movilData.toString()}")
                            resultDireccion.postValue(movilData.data.get(0).label!!)
                            Log.w(TAG, "Response location: ${movilData.data.get(0).label}")
                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG,
                            "Response: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })

            } catch (e: Exception) {
                Log.w(TAG, "Response: ${e}")
            }
        }

    }

    fun enviarAlerta(
        tipoAlerta: String,
        movil: String,
        email: String,
        direccion: String,
        geopos: String
    ) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                Log.w(TAG, "Enviando ALERTA")
                val call: Call<String?>? =
                    RetrofitService.RetrofitInstance.api.enviarAlerta(
                        tipoAlerta,
                        movil,
                        email,
                        direccion,
                        geopos
                    )
                call!!.enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        if (response.isSuccessful()) {
                            Log.w(TAG, "Response ALERTA: ${response.body()}")
                            if (isJsonResponse(response)) {
                                if (response.code() == 200) {
                                    if (tipoAlerta == PRUEBA) {
                                        val jsonResponse = response.body().toString()
                                        val gson = Gson()
                                        var alertResponse =
                                            gson.fromJson(jsonResponse, AlertResponse::class.java)

                                        if (alertResponse.status.toString().contains("PRUEBA")) {
                                            state.postValue(true)
                                        }
                                    } else {
                                        alertState.postValue(true)
                                    }

                                }
                            }

                        } else {
                            Log.w(TAG, "Response ALERTA: error")
                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG,
                            "Response: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })

            } catch (e: Exception) {
                Log.w(TAG, "Response: ${e}")
            }
        }

    }

    fun buscarAlertaSeguimiento(
        tipoAlerta: String,
        movil: String,
        email: String,
        al_movil: String,
    ) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                Log.w(TAG, "Buscando ALERTA")
                val call: Call<String?>? =
                    RetrofitService.RetrofitInstance.api.buscarAlertaSeguimiento(
                        tipoAlerta,
                        movil,
                        email,
                        al_movil
                    )
                call!!.enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        if (response.isSuccessful()) {
                            Log.w(TAG, "Response ALERTA: ${response.body()}")
                            if (isJsonResponse(response)) {
                                if (response.code() == 200) {
                                    val jsonResponse = response.body().toString()
                                    val gson = Gson()
                                    var alertResponse =
                                        gson.fromJson(jsonResponse, AlertResponse::class.java)
                                    resultSeguimiento.postValue(alertResponse)

                                }
                            }

                        } else {
                            Log.w(TAG, "Response ALERTA: error")
                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG,
                            "Response: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })

            } catch (e: Exception) {
                Log.w(TAG, "Response: ${e}")
            }
        }

    }

    private fun isJsonResponse(response: Response<String?>): Boolean {
        return response.body().toString().startsWith("{")
    }
}

