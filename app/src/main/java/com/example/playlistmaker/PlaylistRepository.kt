package com.example.playlistmaker

import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaylistRepository(
    val playlistDao: PlaylistDao,
    val trackDao: TrackDao
) {
    val allPlaylists: LiveData<List<Playlist>> = playlistDao.getAllPlaylists()

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: String, track: Track): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {

            if (!checkPlaylistExists(playlistId)) {
                Log.e("RepoAdd", "Плейлист не существует: $playlistId")
                return@withContext false
            }

            if (!checkTrackExists(trackId)) {
                insertTrack(track)
                Log.d("RepoAdd", "Трек сохранён в базу: ${track.trackName}")
            }


            val playlistTrack = PlaylistTrack(playlistId, trackId)
            val rowId = playlistDao.insertPlaylistTrack(playlistTrack)
            Log.d("RepoAdd", "Связь добавлена: rowId=$rowId, playlistId=$playlistId, trackId=$trackId")

            rowId > 0
        } catch (e: Exception) {
            Log.e("RepoAdd", "Ошибка при добавлении трека в плейлист: ${e.message}", e)
            false
        }
    }

    suspend fun checkTrackExists(trackId: String): Boolean = withContext(Dispatchers.IO) {
        trackDao.trackExists(trackId)
    }

    suspend fun checkPlaylistExists(playlistId: Long): Boolean = withContext(Dispatchers.IO) {
        playlistDao.playlistExists(playlistId)
    }

    suspend fun removeTrackFromPlaylist(playlistTrack: PlaylistTrack): Boolean = withContext(Dispatchers.IO) {
        try {
            playlistDao.deletePlaylistTrack(playlistTrack)
            true
        } catch (e: Exception) {
            Log.e("RepoRemove", "Ошибка delete: ${e.message}", e)
            false
        }
    }

    suspend fun insertTrack(track: Track): Long = withContext(Dispatchers.IO) {
        trackDao.insertTrack(track)
    }

    fun getPlaylistTracks(playlistId: Long): LiveData<List<Track>> {
        return playlistDao.getPlaylistTracks(playlistId)
    }


    fun insert(playlist: Playlist, onResult: (Long) -> Unit) {
        Thread {
            try {
                val playlistId = playlistDao.insertPlaylist(playlist)
                onResult(playlistId)
            } catch (e: Exception) {
                Log.e("PlaylistRepository", "Ошибка создания: ${e.message}", e)
                onResult(-1)
            }
        }.start()
    }
}