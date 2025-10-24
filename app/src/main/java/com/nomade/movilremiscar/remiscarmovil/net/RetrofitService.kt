package com.nomade.movilremiscar.remiscarmovil.net

import com.google.gson.GsonBuilder
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.ALERTA_ADD
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.AUTO_ADD
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.BASE_ADD
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.BUSCAR_ALERTA_ADD
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.COORDENADAS_VIAJE_ADD
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.INICIO_ADD
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.MAIN_GEOPOS_ADD
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.MAIN_VIEW_ADD
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.MENSAJES_ADD
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.MOVIL_PUNTERO_ADD
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.PUNTERO_ALTERNATIVA_ADD
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.PUNTERO_LIBRE_ADD
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.PUNTERO_SALIO_PARADA_ADD
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.PUNTERO_VIAJE_ADD
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.RECLAMO_ADD
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.VALIDAR_USER_ADD
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.POST
import retrofit2.http.Query


interface RetrofitService {

    @POST(VALIDAR_USER_ADD)
    fun getUserStatus(
        @Query("IMEI") imei: String,
        @Query("version") version: String
    ): Call<String?>?

    @POST(ALERTA_ADD)
    fun enviarAlerta(
        @Query("status") status: String,
        @Query("Movil") movil: String,
        @Query("IMEI") imei: String,
        @Query("Ubicacion") ubicacion: String,
        @Query("geopos") geopos: String
    ): Call<String?>?

    @POST(MENSAJES_ADD)
    fun buscarMensajes(
        @Query("IMEI") imei: String
    ): Call<String?>?

    @POST(BUSCAR_ALERTA_ADD)
    fun buscarAlerta(
        @Query("IMEI") imei: String, @Query("status") status: String, @Query("Movil") movil: String,
        @Query("Ubicacion") ubicacion: String,
        @Query("movil_al") movil_al: String,
        @Query("geopos") geopos: String
    ): Call<String?>?

    @POST(BUSCAR_ALERTA_ADD)
    fun buscarAlertaSeguimiento(
        @Query("status") status: String, @Query("Movil") movil: String, @Query("IMEI") imei: String,
        @Query("movil_al") al_movil: String
    ): Call<String?>?

    @POST(AUTO_ADD)
    fun buscarAuto(
        @Query("Movil") movil: String,
        @Query("IMEI") imei: String,
        @Query("geopos") geopos: String
    ): Call<String?>?

    @POST(MOVIL_PUNTERO_ADD)
    fun buscarMovilPuntero(
        @Query("Movil") movil: String,
        @Query("IMEI") imei: String,
        @Query("geopos") geopos: String
    ): Call<String?>?

    @POST(MAIN_GEOPOS_ADD)
    fun enviarGeopos(
        @Query("Movil") movil: String,
        @Query("IMEI") imei: String,
        @Query("geopos") geopos: String
    ): Call<String?>?

    @POST(INICIO_ADD)
    fun cargarInicio(
        @Query("Movil") movil: String,
        @Query("imei") imei: String,
        @Query("geopos") geopos: String
    ): Call<String?>?

    @POST(MAIN_VIEW_ADD)
    fun enviarGeoposMviajeshoy(
        @Query("imei") imei: String,
        @Query("Movil") movil: String,
        @Query("geopos") geopos: String
    ): Call<String?>?

    @POST(PUNTERO_ALTERNATIVA_ADD)
    fun buscarPunteroAlternativa(
        @Query("Movil") movil: String,
        @Query("IMEI") imei: String,
        @Query("geopos") geopos: String
    ): Call<String?>?

    @POST(PUNTERO_LIBRE_ADD)
    fun buscarPunteroLibre(
        @Query("Movil") movil: String,
        @Query("IMEI") imei: String,
        @Query("geopos") geopos: String
    ): Call<String?>?

    @POST(PUNTERO_VIAJE_ADD)
    fun buscarPunteroViaje(
        @Query("Movil") movil: String,
        @Query("IMEI") imei: String,
        @Query("geopos") geopos: String
    ): Call<String?>?

    @POST(PUNTERO_SALIO_PARADA_ADD)
    fun buscarPunteroSalioParada(
        @Query("Movil") movil: String,
        @Query("IMEI") imei: String,
        @Query("geopos") geopos: String
    ): Call<String?>?

    @POST(COORDENADAS_VIAJE_ADD)
    fun buscarCoordenadasViaje(
        @Query("Movil") movil: String
    ): Call<String?>?

    @POST(RECLAMO_ADD)
    fun enviarReclamo(
        @Query("IMEI") imei: String,
        @Query("Celular") celular: String,
        @Query("Descripcion") descripcion: String,
        @Query("Pasajero") pasajero: String
    ): Call<String?>?

    object RetrofitInstance {
        var gson = GsonBuilder()
            .setLenient()
            .create()

        val api: RetrofitService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_ADD)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(RetrofitService::class.java)
        }
    }

}