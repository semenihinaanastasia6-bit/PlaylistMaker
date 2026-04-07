package com.example.playlistmaker

import android.util.Log
import androidx.lifecycle.LiveData

import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class FavoriteTrackRepository(private val favoriteTrackDao: FavoriteTrackDao) {

    fun getAllFavorites(): LiveData<List<FavoriteTrackEntity>> {
        return favoriteTrackDao.getAllFavorites()
    }

    suspend fun addTrackToFavorites(track: FavoriteTrackEntity) {
        withContext(Dispatchers.IO) {
            val result = favoriteTrackDao.addToFavorites(track)
            Log.d("FavoriteTrackRepo", "Track added: ${track.trackName}, ID: $result")
        }
    }

    suspend fun removeTrackFromFavorites(track: FavoriteTrackEntity) {
        withContext(Dispatchers.IO) {
            val result = favoriteTrackDao.removeFromFavorites(track)
            Log.d("FavoriteTrackRepo", "Track removed: ${track.trackName}, Rows affected: $result")
        }
    }
    fun isFavorite(url: String): LiveData<Boolean> {
        return favoriteTrackDao.isFavorite(url)
    }
    suspend fun removeTrackByUrl(previewUrl: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                favoriteTrackDao.deleteByUrl(previewUrl)
            } catch (e: Exception) {
                Log.e("DAO", "Failed to delete track: ${e.message}")
                0
            }
        }
    }




}
