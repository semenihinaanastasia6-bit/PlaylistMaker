package com.example.playlistmaker

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FavoriteTrackViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteTrackViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoriteTrackViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
