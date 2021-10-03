package com.example.anonycall.viewModels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RandomChatViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RandomChatViewModel::class.java)) {
            return RandomChatViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}