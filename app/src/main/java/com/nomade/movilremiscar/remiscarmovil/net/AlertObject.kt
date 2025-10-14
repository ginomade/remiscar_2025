package com.nomade.movilremiscar.remiscarmovil.net

import com.google.gson.annotations.SerializedName

data class AlertObject(
    @SerializedName("result") var result: Int? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("Fecha") var Fecha: String? = null,
    @SerializedName("GeoPos") var GeoPos: String? = null,
    @SerializedName("Ubicacion") var Ubicacion: String? = null,
    @SerializedName("Movil") var Movil: String? = null
)
