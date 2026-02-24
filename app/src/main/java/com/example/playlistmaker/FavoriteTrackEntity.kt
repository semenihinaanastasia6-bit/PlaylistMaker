package com.example.playlistmaker

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "favorite_tracks")
data class FavoriteTrackEntity(
    @PrimaryKey val previewUrl: String,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String?,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val addedAt: Long = System.currentTimeMillis()
)


@Dao
interface FavoriteTrackDao {
    @Query("SELECT * FROM favorite_tracks ORDER BY addedAt DESC")
    fun getAllFavorites(): LiveData<List<FavoriteTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addToFavorites(track: FavoriteTrackEntity): Long

    @Delete
    fun removeFromFavorites(track: FavoriteTrackEntity): Int

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_tracks WHERE previewUrl = :url)")
    fun isFavorite(url: String): LiveData<Boolean>

    @Query("DELETE FROM favorite_tracks WHERE previewUrl = :previewUrl")
    fun deleteByUrl(previewUrl: String): Int
}