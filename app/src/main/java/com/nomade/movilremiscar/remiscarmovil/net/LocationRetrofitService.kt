package com.nomade.movilremiscar.remiscarmovil.net

import com.google.gson.GsonBuilder
import com.nomade.movilremiscar.remiscarmovil.utils.Constants.LOCATION_ADD
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface LocationRetrofitService {

    @GET("reverse")
    fun getAddress(@Query("access_key") key: String, @Query("query") geo: String): Call<String?>?

    object LocationRetrofitInstance {
        var gson = GsonBuilder()
            .setLenient()
            .create()

        val api: LocationRetrofitService by lazy {
            Retrofit.Builder()
                .baseUrl(LOCATION_ADD)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(LocationRetrofitService::class.java)
        }
    }

}