package com.example.try3.data.supabase

import com.example.try3.data.model.Record
import retrofit2.http.*

interface SupabaseApi {
    @GET("records")
    fun getAllRecords(): retrofit2.Call<List<Record>>

    @POST("records")
    fun insertRecord(@Body record: Record): retrofit2.Call<Record>

    @PATCH("records")
    fun updateRecord(
        @Body record: Record,
        @Query("id") eq: String
    ): retrofit2.Call<Record>

    @DELETE("records")
    fun deleteRecord(@Query("id") eq: String): retrofit2.Call<Unit>
}
