package com.example.try3.data.supabase

import android.content.ContentResolver
import android.net.Uri
import com.example.try3.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SupabaseStorage {
    private val okHttpClient = OkHttpClient.Builder().build()

    suspend fun uploadImage(uri: Uri, contentResolver: ContentResolver): String? {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "image_${timestamp}_${System.currentTimeMillis()}.jpg"

                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@withContext null
                inputStream.close()

                val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

                val request = okhttp3.Request.Builder()
                    .url("${BuildConfig.SUPABASE_URL}/storage/v1/object/images/$fileName")
                    .header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                    .header("Content-Type", "image/jpeg")
                    .header("x-upsert", "false")
                    .put(requestBody)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/images/$fileName"
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

object App {
    lateinit var context: android.content.Context
}
