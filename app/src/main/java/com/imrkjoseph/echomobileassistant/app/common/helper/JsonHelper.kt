package com.imrkjoseph.echomobileassistant.app.common.helper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object JsonHelper {

    fun <T : Any> toJson(value: T) : String {
        val baseType = object : TypeToken<T>() {}.type
        return Gson().toJson(value, baseType)
    }

    fun <T : Any> toJsonList(value: List<T>) : String {
        val baseType = object : TypeToken<List<T>>() {}.type
        return Gson().toJson(value, baseType)
    }

    fun <T : Any> fromJson(value : String?) : T {
        val baseType = object : TypeToken<T>() {}.type
        return Gson().fromJson(value, baseType)
    }

    fun <T : Any> fromJsonList(value : String?) : MutableList<T> {
        val baseType = object : TypeToken<List<T>>() {}.type
        return Gson().fromJson(value, baseType)
    }
}