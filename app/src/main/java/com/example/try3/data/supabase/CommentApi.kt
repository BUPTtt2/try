package com.example.try3.data.supabase

import com.example.try3.BuildConfig
import com.example.try3.data.model.Comment
import com.example.try3.data.model.CommentRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class CommentApi {
    private val gson = Gson()
    private val baseUrl = BuildConfig.SUPABASE_URL
    private val apiKey = BuildConfig.SUPABASE_ANON_KEY

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getComments(recordId: Long): List<Comment> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/comments?record_id=eq.$recordId&order=created_at.asc"

            val request = Request.Builder()
                .url(url)
                .header("apikey", apiKey)
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: "[]"

            if (response.isSuccessful) {
                gson.fromJson(body, Array<Comment>::class.java).toList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addComment(recordId: Long, content: String): Comment? = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/comments"

            val commentRequest = CommentRequest(
                recordId = recordId,
                content = content,
                author = "我"
            )

            val jsonBody = gson.toJson(commentRequest)
            val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

            val request = Request.Builder()
                .url(url)
                .header("apikey", apiKey)
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: "[]"

            if (response.isSuccessful && body.isNotEmpty() && body != "[]") {
                val comments = gson.fromJson(body, Array<Comment>::class.java)
                comments.firstOrNull()
            } else {
                Comment(id = System.currentTimeMillis(), recordId = recordId, content = content, author = "我")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteComment(commentId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/comments?id=eq.$commentId"

            val request = Request.Builder()
                .url(url)
                .header("apikey", apiKey)
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .delete()
                .build()

            val response = okHttpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}