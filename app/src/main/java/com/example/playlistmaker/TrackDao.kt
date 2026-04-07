package com.example.playlistmaker


import androidx.room.Dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTrack(track: Track): Long
    @Query("SELECT COUNT(*) > 0 FROM tracks WHERE trackId = :trackId")
    fun trackExists(trackId: String): Boolean
}


