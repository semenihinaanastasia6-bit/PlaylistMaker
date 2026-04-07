package com.example.playlistmaker

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import android.util.Log
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FavoriteTrackEntity::class, PlaylistTrack::class, Track::class, Playlist::class], version = 6, exportSchema = true)

abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteTrackDao(): FavoriteTrackDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null


        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("ALTER TABLE favorite_tracks ADD COLUMN trackId TEXT")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_favorite_tracks_trackId ON favorite_tracks(trackId)")
                database.execSQL("ALTER TABLE favorite_tracks DROP PRIMARY KEY")
                database.execSQL("ALTER TABLE favorite_tracks ADD PRIMARY KEY (trackId)")
            }
        }


        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL("ALTER TABLE favorite_tracks ADD COLUMN addedAt INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {
                    Log.w("Migration", "Column addedAt already exists")
                }
            }
        }


        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "playlist_maker.db"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
