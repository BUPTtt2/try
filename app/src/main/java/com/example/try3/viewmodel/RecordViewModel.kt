package com.example.try3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.try3.data.model.Record
import com.example.try3.data.repository.RecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecordViewModel(private val repository: RecordRepository) : ViewModel() {
    private val _records = MutableStateFlow<List<Record>>(emptyList())
    val records: StateFlow<List<Record>> = _records.asStateFlow()

    init {
        loadRecords()
    }

    fun loadRecords() {
        viewModelScope.launch {
            repository.getAllRecords().collect { recordList ->
                _records.value = recordList
            }
        }
    }

    fun addRecord(record: Record) {
        viewModelScope.launch {
            repository.insertRecord(record)
            loadRecords()
        }
    }

    fun updateRecord(record: Record) {
        viewModelScope.launch {
            repository.updateRecord(record)
        }
    }

    fun deleteRecord(record: Record) {
        viewModelScope.launch {
            repository.deleteRecord(record)
            loadRecords()
        }
    }
}
