package com.example.try3.data.model

import com.google.gson.annotations.SerializedName

data class Comment(
    val id: Long = 0,
    @SerializedName("record_id")
    val recordId: Long,
    val content: String,
    val author: String = "我",
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class CommentRequest(
    @SerializedName("record_id")
    val recordId: Long,
    val content: String,
    val author: String = "我"
)