package com.example.playlistmaker

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query



@Dao
interface PlaylistDao {
    @Insert
    fun insertPlaylist(playlist: Playlist): Long

    @Insert
    fun insertPlaylistTrack(playlistTrack: PlaylistTrack): Long

    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): LiveData<List<Playlist>>
    @Delete
    fun deletePlaylistTrack(playlistTrack: PlaylistTrack)
    @Delete
    fun deletePlaylist(playlist: Playlist)
    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = :playlistId")
    fun getPlaylistTracksCount(playlistId: Long): Int

    @Query("""
    SELECT * FROM tracks 
    WHERE trackId IN (
        SELECT trackId 
        FROM playlist_tracks 
        WHERE playlistId = :playlistId
    )
""")
    fun getPlaylistTracks(playlistId: Long): LiveData<List<Track>>


    @Query("SELECT EXISTS(SELECT 1 FROM playlists WHERE playlistId = :playlistId)")
    fun playlistExists(playlistId: Long): Boolean
}
