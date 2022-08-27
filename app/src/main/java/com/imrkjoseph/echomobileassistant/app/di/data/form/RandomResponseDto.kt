package com.imrkjoseph.echomobileassistant.app.di.data.form

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
class RandomResponseDto {

    @SerializedName("random")
    var randomResponse: MutableList<String>? = null
}