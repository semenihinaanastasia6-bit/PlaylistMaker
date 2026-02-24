package com.example.playlistmaker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteTrackViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FavoriteTrackRepository

    init {
        val db = AppDatabase.getDatabase(application)
        val dao = db.favoriteTrackDao()
        repository = FavoriteTrackRepository(dao)
    }

    fun addTrackToFavorites(track: FavoriteTrackEntity) {
        viewModelScope.launch {
            repository.addTrackToFavorites(track)
        }
    }

    fun removeTrackFromFavorites(track: FavoriteTrackEntity) {
        viewModelScope.launch {
            repository.removeTrackFromFavorites(track)
        }
    }

    fun isFavorite(url: String): LiveData<Boolean> {
        return repository.isFavorite(url)
    }
    fun getFavoriteTracks(): LiveData<List<Track>> {
        val result = MutableLiveData<List<Track>>()

        viewModelScope.launch {
            try {
                val entitiesLiveData = repository.getAllFavorites()
                entitiesLiveData.observeForever { entities ->
                    val tracks = entities.map { it.toTrack() }
                    result.postValue(tracks)
                }
            } catch (e: Exception) {
                result.postValue(emptyList())
            }
        }

        return result
    }

    fun removeTrackFromFavoritesByUrl(previewUrl: String) {
        viewModelScope.launch {
            repository.removeTrackByUrl(previewUrl)
        }
    }
    suspend fun removeTrackByUrl(previewUrl: String): Int {
        return repository.removeTrackByUrl(previewUrl)
    }
}
