package com.nomade.movilremiscar.remiscarmovil.net

import com.google.gson.annotations.SerializedName

data class ResultObject (
    @SerializedName("result"  ) var result  : Int?    = null,
    @SerializedName("message" ) var message : String? = null,
    @SerializedName("movil"   ) var movil   : String? = null
)
