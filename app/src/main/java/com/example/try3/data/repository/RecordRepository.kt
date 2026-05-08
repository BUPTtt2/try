package com.example.try3.data.repository

import com.example.try3.data.model.Record
import com.example.try3.data.supabase.SupabaseApi
import com.example.try3.data.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class RecordRepository {
    private val api: SupabaseApi = SupabaseClient.retrofit.create(SupabaseApi::class.java)

    fun getAllRecords(): Flow<List<Record>> = flow {
        try {
            val response = api.getAllRecords().execute()
            if (response.isSuccessful) {
                val records = response.body() ?: emptyList()
                emit(records.sortedByDescending { it.getDate() })
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun insertRecord(record: Record) {
        withContext(Dispatchers.IO) {
            try {
                api.insertRecord(record).execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun updateRecord(record: Record) {
        withContext(Dispatchers.IO) {
            try {
                record.id?.let { id ->
                    api.updateRecord(record, "eq.$id").execute()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteRecord(record: Record) {
        withContext(Dispatchers.IO) {
            try {
                record.id?.let { id ->
                    api.deleteRecord("eq.$id").execute()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
