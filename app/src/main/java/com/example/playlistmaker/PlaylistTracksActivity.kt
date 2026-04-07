package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class PlaylistTracksActivity : AppCompatActivity() {
    private lateinit var rvPlaylistTracks: RecyclerView
    private lateinit var playlistName: TextView
    private lateinit var emptyStateImage: ImageView
    private lateinit var emptyStateText: TextView
    private var adapter: TrackAdapter? = null

    private lateinit var viewModel: PlaylistViewModel

    private var playlistId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_tracks)


        rvPlaylistTracks = findViewById(R.id.rv_playlist_tracks)
        playlistName = findViewById(R.id.playlistName)
        emptyStateImage = findViewById(R.id.emptyStateImage)
        emptyStateText = findViewById(R.id.emptyStateText)


        playlistId = intent.getLongExtra("PLAYLIST_ID", -1)
        Log.d("PlaylistTracks", "Получен playlistId: $playlistId")
        val playlistNameExtra = intent.getStringExtra("PLAYLIST_NAME") ?: "Плейлист"
        playlistName.text = playlistNameExtra

        setupViewModel()
        setupRecyclerView()
        loadPlaylistTracks()


        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[PlaylistViewModel::class.java]
    }
    private fun openAudioPlayer(track: Track) {
        val intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra(AudioPlayerActivity.EXTRA_TRACK, track)
        startActivity(intent)
    }

    private fun setupRecyclerView() {
        adapter = TrackAdapter(emptyList()) { track ->
            openAudioPlayer(track)
        }
        rvPlaylistTracks.layoutManager = LinearLayoutManager(this)
        rvPlaylistTracks.adapter = adapter
    }


    private fun loadPlaylistTracks() {
        if (playlistId != -1L) {
            Log.d("DB_DEBUG", "Запрашиваем треки для playlistId: $playlistId")

            viewModel.getPlaylistTracks(playlistId).observe(this) { tracks ->
                Log.d("DB_DEBUG", "Получено треков: ${tracks.size}")
                tracks.forEach { track ->
                    Log.d("DB_DEBUG", "Трек: ${track.trackName} — ${track.trackId}")
                }

                if (tracks.isNotEmpty()) {
                    adapter?.submitList(tracks)
                    updateEmptyState(false)
                } else {
                    updateEmptyState(true)
                }
            }
        } else {
            Log.e("DB_DEBUG", "playlistId == -1 — не можем загрузить треки")
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            rvPlaylistTracks.visibility = View.GONE
            emptyStateImage.visibility = View.VISIBLE
            emptyStateText.visibility = View.VISIBLE
        } else {
            rvPlaylistTracks.visibility = View.VISIBLE
            emptyStateImage.visibility = View.GONE
            emptyStateText.visibility = View.GONE
        }
    }

}

