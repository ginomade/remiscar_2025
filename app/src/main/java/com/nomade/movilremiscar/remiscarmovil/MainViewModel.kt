package com.nomade.movilremiscar.remiscarmovil

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.nomade.movilremiscar.remiscarmovil.net.AlertObject
import com.nomade.movilremiscar.remiscarmovil.net.JsonPuntero
import com.nomade.movilremiscar.remiscarmovil.net.LocationData
import com.nomade.movilremiscar.remiscarmovil.net.LocationRetrofitService
import com.nomade.movilremiscar.remiscarmovil.net.ResultObject
import com.nomade.movilremiscar.remiscarmovil.net.RetrofitService
import com.nomade.movilremiscar.remiscarmovil.net.ViajeResultObject
import com.nomade.movilremiscar.remiscarmovil.utils.Constants
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.COORDENADAS_VIAJE_KEY
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.SESION_INICIADA
import com.nomade.movilremiscar.remiscarmovil.utils.SharedPrefsUtil
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainViewModel : ViewModel() {
    private val userInfo = MutableLiveData<String>()
    private val userStatus = MutableLiveData<String>()
    private val resultMensaje = MutableLiveData<String>()
    private val resultAlerta = MutableLiveData<AlertObject>()
    private val resultDireccion = MutableLiveData<String>()
    private val resultAuto = MutableLiveData<JsonPuntero>()
    private val resultMovilPuntero = MutableLiveData<JsonPuntero>()
    private val resultPunteroAlt = MutableLiveData<JsonPuntero>()
    private val resultPunteroLibre = MutableLiveData<JsonPuntero>()
    private val resultPunteroViaje = MutableLiveData<JsonPuntero>()
    private val resultPunteroSalioParada = MutableLiveData<JsonPuntero>()

    private var stateRunning = false
    private var stateJob: Job? = null

    var TAG_VIEWMODEL = "MainViewModel"


    override fun onCleared() {
        super.onCleared()
        stateJob?.cancel()
        stateRunning = false
    }


    fun getUserInfo(): LiveData<String> {
        return userInfo
    }

    fun getUserStatus(): LiveData<String> {
        return userStatus
    }

    fun getResultMensaje(): LiveData<String> {
        return resultMensaje
    }

    fun getResultAlerta(): LiveData<AlertObject> {
        return resultAlerta
    }

    fun getDir(): LiveData<String> {
        return resultDireccion
    }

    fun getResultAuto(): LiveData<JsonPuntero> {
        return resultAuto
    }

    fun getResultMovilPuntero(): LiveData<JsonPuntero> {
        return resultMovilPuntero
    }

    fun getResultPunteroAlt(): LiveData<JsonPuntero> {
        return resultPunteroAlt
    }

    fun getResultPunteroLibre(): LiveData<JsonPuntero> {
        return resultPunteroLibre
    }

    fun getResultPunteroViaje(): LiveData<JsonPuntero> {
        return resultPunteroViaje
    }

    fun getResultPunteroSalioParada(): LiveData<JsonPuntero> {
        return resultPunteroSalioParada
    }

    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.w(TAG_VIEWMODEL, "Exception handled: ${throwable.localizedMessage}")
    }

    fun validarUsuario(userEmail: String) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                Log.w(TAG_VIEWMODEL, "enviando - validarUsuario")
                val version = SharedPrefsUtil.get(Constants.VERSION_NAME_KEY, "")
                val call: Call<String?>? =
                    RetrofitService.RetrofitInstance.api.getUserStatus(userEmail, version)
                call!!.enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        if (response.isSuccessful()) {
                            Log.w(TAG_VIEWMODEL, "Response: ${response.body()}")
                            var movilData = obtenerJsonResultObject(response)
                            userInfo.postValue(movilData?.movil!!)
                            userStatus.postValue(movilData.result!!.toString())
                        }

                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG_VIEWMODEL,
                            "Response: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })

            } catch (e: Exception) {
                Log.w(TAG_VIEWMODEL, "Response: ${e}")
            }
        }

    }

    fun buscarMensajes(userEmail: String, movil: String) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                Log.w(TAG_VIEWMODEL, "enviando - buscarMensajes ${userEmail}")
                val call: Call<String?>? =
                    RetrofitService.RetrofitInstance.api.buscarMensajes(userEmail)
                call!!.enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        if (response.isSuccessful() && isJsonResponse(response)) {
                            Log.w(TAG_VIEWMODEL, "buscarMensajes: ${response.body()}")
                            var movilData = obtenerResultObject(response)
                            resultMensaje.postValue(movilData?.result!!.toString())
                        }

                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG_VIEWMODEL,
                            "buscarMensajes: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })

            } catch (e: Exception) {
                Log.w(TAG_VIEWMODEL, "buscarMensajes: ${e}")
            }
        }

    }

    fun buscarAlerta(userEmail: String, movil: String, direccion: String, geopos: String) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                Log.w(TAG_VIEWMODEL, "enviando - buscarAlerta")
                val call: Call<String?>? =
                    RetrofitService.RetrofitInstance.api.buscarAlerta(
                        userEmail,
                        "",
                        movil,
                        direccion,
                        "",
                        geopos
                    )
                call!!.enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        if (response.isSuccessful() && isJsonResponse(response)) {
                            Log.w(TAG_VIEWMODEL, "buscarAlerta: ${response.body()}")
                            val jsonResponse = response.body().toString()
                            val gson = Gson()
                            var movilData = gson.fromJson(jsonResponse, AlertObject::class.java)
                            resultAlerta.postValue(movilData)
                        }

                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG_VIEWMODEL,
                            "buscarAlerta: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })

            } catch (e: Exception) {
                Log.w(TAG_VIEWMODEL, "buscarAlerta: ${e}")
            }
        }

    }

    fun buscarDireccion(geopos: String) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                val call: Call<String?>? =
                    LocationRetrofitService.LocationRetrofitInstance.api.getAddress(
                        Constants.POSITION_KEY,
                        geopos
                    )
                call!!.enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        Log.w(TAG_VIEWMODEL, "Response: ${response.isSuccessful}")
                        if (response.isSuccessful() && isJsonResponse(response)) {
                            Log.w(TAG_VIEWMODEL, "Response: ${response.body()}")
                            val jsonResponse = response.body().toString()
                            val gson = Gson()
                            var movilData = gson.fromJson(jsonResponse, LocationData::class.java)
                            Log.w(TAG_VIEWMODEL, "Response location: ${movilData.toString()}")
                            resultDireccion.postValue(movilData.data.get(0).label!!)
                            Log.w(
                                TAG_VIEWMODEL,
                                "Response location: ${movilData.data.get(0).label}"
                            )
                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG_VIEWMODEL,
                            "Response: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })

            } catch (e: Exception) {
                Log.w(TAG_VIEWMODEL, "Response: ${e}")
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
                        if (response.isSuccessful() && isJsonResponse(response)) {
                            Log.w(TAG_VIEWMODEL, "Response ALERTA: ${response.body()}")
                            val jsonResponse = response.body().toString()
                            val gson = Gson()
                            var movilData =
                                gson.fromJson(jsonResponse, ResultObject::class.java)
                            var result = movilData.result
                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG_VIEWMODEL,
                            "Response: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })

            } catch (e: Exception) {
                Log.w(TAG_VIEWMODEL, "Response: ${e}")
            }
        }

    }

    fun buscarAuto(userEmail: String, movil: String, geopos: String) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                Log.w(
                    TAG_VIEWMODEL,
                    "enviando - buscarAuto - email: ${userEmail} , movil: ${movil}, geo: ${geopos}"
                )
                val call: Call<String?>? =
                    RetrofitService.RetrofitInstance.api.buscarAuto(movil, userEmail, geopos)
                call!!.enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        if (response.isSuccessful() && isJsonResponse(response)) {
                            Log.w(TAG_VIEWMODEL, "buscarAuto: ${response.body()}")

                            var movilData = obtenerRespuestaPuntero(response)
                            resultAuto.postValue(movilData!!)
                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG_VIEWMODEL,
                            "buscarAuto: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.w(TAG_VIEWMODEL, "buscarAuto: ${e}")
            }
        }
    }

    fun buscarMovilPuntero(userEmail: String, movil: String, geopos: String) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                Log.w(
                    TAG_VIEWMODEL,
                    "enviando - buscarMovilPuntero - email: ${userEmail} , movil: ${movil}, geo: ${geopos}"
                )
                val call: Call<String?>? =
                    RetrofitService.RetrofitInstance.api.buscarMovilPuntero(
                        movil,
                        userEmail,
                        geopos
                    )
                call!!.enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        if (response.isSuccessful() && isJsonResponse(response)) {
                            Log.w(TAG_VIEWMODEL, "buscarMovilPuntero: ${response.body()}")

                            var movilData = obtenerRespuestaPuntero(response)
                            resultMovilPuntero.postValue(movilData!!)
                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG_VIEWMODEL,
                            "buscarMovilPuntero: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.w(TAG_VIEWMODEL, "buscarMovilPuntero: ${e}")
            }
        }
    }

    fun buscarPunteroAlternativa(userEmail: String, movil: String, geopos: String) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                Log.w(
                    TAG_VIEWMODEL,
                    "enviando - buscarPunteroAlternativa - email: ${userEmail} , movil: ${movil}, geo: ${geopos}"
                )
                val call: Call<String?>? =
                    RetrofitService.RetrofitInstance.api.buscarPunteroAlternativa(
                        movil,
                        userEmail,
                        geopos
                    )
                call!!.enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        if (response.isSuccessful() && isJsonResponse(response)) {
                            Log.w(TAG_VIEWMODEL, "buscarPunteroAlternativa: ${response.body()}")

                            var movilData = obtenerRespuestaPuntero(response)
                            resultPunteroAlt.postValue(movilData!!)
                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG_VIEWMODEL,
                            "buscarPunteroAlternativa: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.w(TAG_VIEWMODEL, "buscarPunteroAlternativa: ${e}")
            }
        }
    }

    fun buscarPunteroLibre(userEmail: String, movil: String, geopos: String) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                Log.w(
                    TAG_VIEWMODEL,
                    "enviando - buscarPunteroLibre - email: ${userEmail} , movil: ${movil}, geo: ${geopos}"
                )
                val call: Call<String?>? =
                    RetrofitService.RetrofitInstance.api.buscarPunteroLibre(
                        movil,
                        userEmail,
                        geopos
                    )
                call!!.enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        if (response.isSuccessful() && isJsonResponse(response)) {
                            Log.w(TAG_VIEWMODEL, "buscarPunteroLibre: ${response.body()}")

                            var movilData = obtenerRespuestaPuntero(response)
                            resultPunteroLibre.postValue(movilData!!)
                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG_VIEWMODEL,
                            "buscarPunteroLibre: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.w(TAG_VIEWMODEL, "buscarPunteroLibre: ${e}")
            }
        }
    }

    fun buscarPunteroViaje(userEmail: String, movil: String, geopos: String) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                Log.w(
                    TAG_VIEWMODEL,
                    "enviando - buscarPunteroViaje - email: ${userEmail} , movil: ${movil}, geo: ${geopos}"
                )
                val call: Call<String?>? =
                    RetrofitService.RetrofitInstance.api.buscarPunteroViaje(
                        movil,
                        userEmail,
                        geopos
                    )
                call!!.enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        if (response.isSuccessful() && isJsonResponse(response)) {
                            Log.w(TAG_VIEWMODEL, "buscarPunteroViaje: ${response.body()}")

                            var movilData = obtenerRespuestaPuntero(response)
                            resultPunteroViaje.postValue(movilData!!)
                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG_VIEWMODEL,
                            "buscarPunteroViaje: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.w(TAG_VIEWMODEL, "buscarPunteroViaje: ${e}")
            }
        }
    }

    fun buscarPunteroSalioParada(userEmail: String, movil: String, geopos: String) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                Log.w(
                    TAG_VIEWMODEL,
                    "enviando - buscarPunteroSalioParada - email: ${userEmail} , movil: ${movil}, geo: ${geopos}"
                )
                val call: Call<String?>? =
                    RetrofitService.RetrofitInstance.api.buscarPunteroSalioParada(
                        movil,
                        userEmail,
                        geopos
                    )
                call!!.enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        if (response.isSuccessful() && isJsonResponse(response)) {
                            Log.w(TAG_VIEWMODEL, "buscarPunteroSalioParada: ${response.body()}")

                            var movilData = obtenerRespuestaPuntero(response)
                            resultPunteroSalioParada.postValue(movilData!!)
                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG_VIEWMODEL,
                            "buscarPunteroSalioParada: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.w(TAG_VIEWMODEL, "buscarPunteroSalioParada: ${e}")
            }
        }
    }

    private fun obtenerRespuestaPuntero(response: Response<String?>): JsonPuntero? {
        val str = response.body().toString()
        val index = str.indexOf("}");
        val jsonResponse = str.substring(0, index + 1)
        val gson = Gson()
        var movilData = gson.fromJson(jsonResponse, JsonPuntero::class.java)
        return movilData
    }

    fun buscarCoordenadasViaje(userEmail: String, movil: String) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                Log.w(TAG_VIEWMODEL, "enviando - buscarCoordenadasViaje")
                val call: Call<String?>? =
                    RetrofitService.RetrofitInstance.api.buscarCoordenadasViaje(movil)
                call!!.enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        if (response.isSuccessful() && isJsonResponse(response)) {
                            Log.w(TAG_VIEWMODEL, "buscarCoordenadasViaje: ${response.body()}")
                            val jsonResponse = response.body().toString()
                            if (jsonResponse.length > 5) {
                                val gson = Gson()
                                try {
                                    var movilData =
                                        gson.fromJson(jsonResponse, ViajeResultObject::class.java)
                                    if (!movilData.coordenadas.isNullOrEmpty()) {
                                        SharedPrefsUtil.set(
                                            COORDENADAS_VIAJE_KEY,
                                            movilData.coordenadas
                                        )
                                    }
                                } catch (e: JsonSyntaxException) {
                                    Log.w(
                                        TAG_VIEWMODEL,
                                        "buscarCoordenadasViaje: JsonSyntaxException"
                                    )
                                }

                            } else {
                                SharedPrefsUtil.set(
                                    COORDENADAS_VIAJE_KEY,
                                    ""
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG_VIEWMODEL,
                            "buscarCoordenadasViaje: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })

            } catch (e: Exception) {
                Log.w(TAG_VIEWMODEL, "buscarCoordenadasViaje: ${e}")
            }
        }

    }

    fun enviarGeopos(userEmail: String, movil: String, geopos: String) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                Log.w(
                    TAG_VIEWMODEL,
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
                            Log.w(TAG_VIEWMODEL, "enviarGeopos: ${response.code()}")
                            val jsonResponse = response.body().toString()
                            if (jsonResponse.length > 5) {
                                val gson = Gson()
                                try {
                                    val jsonObject = JSONObject(jsonResponse)
                                    val sesionIniciada: Boolean =
                                        jsonObject.getBoolean("sesionIniciada")
                                    SharedPrefsUtil.set(
                                        SESION_INICIADA,
                                        sesionIniciada
                                    )
                                } catch (e: JsonSyntaxException) {
                                    Log.w(
                                        TAG_VIEWMODEL,
                                        "enviarGeopos: JsonSyntaxException"
                                    )
                                }

                            }
                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG_VIEWMODEL,
                            "enviarGeopos: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.w(TAG_VIEWMODEL, "enviarGeopos: ${e}")
            }
        }
    }

    fun enviarGeoposMviajeshoy(userEmail: String, movil: String, geopos: String) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                Log.w(
                    TAG_VIEWMODEL,
                    "enviando - enviarGeoposMviajeshoy - email: ${userEmail} , movil: ${movil}, geo: ${geopos}"
                )
                val call: Call<String?>? =
                    RetrofitService.RetrofitInstance.api.enviarGeoposMviajeshoy(
                        userEmail,
                        movil,
                        geopos
                    )
                call!!.enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        if (response.isSuccessful()) {
                            Log.w(TAG_VIEWMODEL, "enviarGeoposMviajeshoy: ${response.code()}")

                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.w(
                            TAG_VIEWMODEL,
                            "enviarGeoposMviajeshoy: error - ${call.request()} , ${t.localizedMessage}"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.w(TAG_VIEWMODEL, "enviarGeoposMviajeshoy: ${e}")
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentTimeString(): String {
        // Get current date and time
        val currentDateTime = LocalDateTime.now()

        // Format the current date and time to a string using a specific format
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedDateTime = currentDateTime.format(formatter)

        return formattedDateTime
    }

    private fun obtenerResultObject(response: Response<String?>): ResultObject? {
        val str = response.body().toString()
        val index = str.indexOf("}");
        val jsonResponse = str.substring(0, index + 1)
        val gson = Gson()
        var movilData = gson.fromJson(jsonResponse, ResultObject::class.java)
        return movilData
    }

    private fun obtenerJsonResultObject(response: Response<String?>): ResultObject? {
        val str = response.body().toString()
        val index = str.indexOf("{");
        val indexFin = str.indexOf("}");
        val jsonResponse = str.substring(index, indexFin + 1)
        val gson = Gson()
        var movilData = gson.fromJson(jsonResponse, ResultObject::class.java)
        return movilData
    }

    private fun isJsonResponse(response: Response<String?>): Boolean {
        return response.body().toString().startsWith("{")
    }

}

