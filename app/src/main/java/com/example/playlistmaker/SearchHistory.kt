package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

class SearchHistory(private val sharedPreferences: SharedPreferences) {
    private val gson = Gson()
    private val maxHistorySize = 10

    fun getHistory(): List<Track> {
        val json = sharedPreferences.getString("search_history", null)
        return if (json != null) {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun saveTrack(track: Track) {
        val history = getHistory().toMutableList()
        history.remove(track)
        history.add(0, track)

        if (history.size > maxHistorySize) {
            history.removeAt(history.size - 1)
        }

        sharedPreferences.edit { putString("search_history", gson.toJson(history)) }
    }

    fun clearHistory() {
        sharedPreferences.edit { remove("search_history") }
    }

    fun contains(track: Track): Boolean {
        val history = getHistory()
        return history.any { it.trackName == track.trackName && it.artistName == track.artistName }
    }



}
