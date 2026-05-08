package com.example.try3.data.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Record(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("content")
    val content: String,
    @SerializedName("location")
    val location: String? = null,
    @SerializedName("latitude")
    val latitude: Double? = null,
    @SerializedName("longitude")
    val longitude: Double? = null,
    @SerializedName("tag")
    val tag: String? = null,
    @SerializedName("image_uri")
    val imageUri: String? = null,
    @SerializedName("timestamp")
    val timestamp: String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
) {
    fun getImageUriList(): List<String> {
        if (imageUri.isNullOrBlank()) return emptyList()
        return imageUri.split(",").filter { it.isNotBlank() }
    }

    fun getDate(): Date {
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(timestamp) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
}
