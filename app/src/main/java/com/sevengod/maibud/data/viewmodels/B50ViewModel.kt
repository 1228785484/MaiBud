package com.sevengod.maibud.data.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sevengod.maibud.data.entities.RecordEntity
import com.sevengod.maibud.repository.QRCodeRepository
import com.sevengod.maibud.repository.RecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class B50ViewModel(
    private val repository: RecordRepository,
    private val applicationContext: Context
) : ViewModel() {
    fun getB50RecordDataFlow(context: Context): Flow<List<RecordEntity>> =
        repository.getB50RecordDataFlow(applicationContext)
//            .onStart {
//                Log.d("B50ViewModel", "onStart triggered")
//            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

}
class B50ViewModelFactory(
    private val repository: RecordRepository,
    private val applicationContext: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(B50ViewModel::class.java)) {
            return B50ViewModel(repository, applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 