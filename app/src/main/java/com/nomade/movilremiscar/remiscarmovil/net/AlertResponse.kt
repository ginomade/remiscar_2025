package com.nomade.movilremiscar.remiscarmovil.net

import com.google.gson.annotations.SerializedName

data class AlertResponse(
    @SerializedName("status"  ) var status  : String?    = null,
    @SerializedName("Fecha" ) var fecha : String? = null,
    @SerializedName("Movil"   ) var movil   : String? = null,
    @SerializedName("Ubicacion"   ) var ubicacion   : String? = null,
    @SerializedName("geopos"   ) var geopos   : String? = null
)
