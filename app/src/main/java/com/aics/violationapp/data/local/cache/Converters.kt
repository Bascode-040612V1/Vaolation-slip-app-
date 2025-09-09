package com.aics.violationapp.data.local.cache

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return if (value == null) null else Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(value, listType)
        }
    }
    
    @TypeConverter
    fun fromStringMap(value: Map<String, Any>?): String? {
        return if (value == null) null else Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringMap(value: String?): Map<String, Any>? {
        return if (value == null) null else {
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            Gson().fromJson(value, mapType)
        }
    }
}