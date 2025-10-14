package com.nomade.movilremiscar.remiscarmovil.net

import com.google.gson.annotations.SerializedName

data class LocationElement (

    @SerializedName("latitude"            ) var latitude           : Double? = null,
    @SerializedName("longitude"           ) var longitude          : Double? = null,
    @SerializedName("type"                ) var type               : String? = null,
    @SerializedName("distance"            ) var distance           : Double? = null,
    @SerializedName("name"                ) var name               : String? = null,
    @SerializedName("number"              ) var number             : String? = null,
    @SerializedName("postal_code"         ) var postalCode         : String? = null,
    @SerializedName("street"              ) var street             : String? = null,
    @SerializedName("confidence"          ) var confidence         : Double? = null,
    @SerializedName("region"              ) var region             : String? = null,
    @SerializedName("region_code"         ) var regionCode         : String? = null,
    @SerializedName("county"              ) var county             : String? = null,
    @SerializedName("locality"            ) var locality           : String? = null,
    @SerializedName("administrative_area" ) var administrativeArea : String? = null,
    @SerializedName("neighbourhood"       ) var neighbourhood      : String? = null,
    @SerializedName("country"             ) var country            : String? = null,
    @SerializedName("country_code"        ) var countryCode        : String? = null,
    @SerializedName("continent"           ) var continent          : String? = null,
    @SerializedName("label"               ) var label              : String? = null

)