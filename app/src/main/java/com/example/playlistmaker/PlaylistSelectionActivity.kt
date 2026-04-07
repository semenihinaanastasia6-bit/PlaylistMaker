package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.ViewModelProvider
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView




class PlaylistSelectionActivity : AppCompatActivity() {
    private lateinit var btnNewPlaylist: Button
    private lateinit var rvPlaylists: RecyclerView
    private lateinit var playlistAdapter: PlaylistSelectionAdapter
    private lateinit var playlistViewModel: PlaylistViewModel
    private var trackId: String = ""
    private var selectedTrack: Track? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_selection)

        btnNewPlaylist = findViewById(R.id.btnNewPlaylist)
        rvPlaylists = findViewById(R.id.rvPlaylists)

        trackId = intent.getStringExtra("trackId") ?: ""
        selectedTrack = intent.getParcelableExtra("track")

        if (trackId.isEmpty() || selectedTrack == null) {
            Log.e("PlaylistSelection", "trackId или track пустой")
            Toast.makeText(this, "Ошибка: трек не выбран", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        playlistViewModel = ViewModelProvider(this)[PlaylistViewModel::class.java]


        setupRecyclerView()


        playlistViewModel.uiState.observe(this) { state ->
            when (state) {
                is UiState.Success -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is UiState.Error -> Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                else -> {}
            }
        }

        btnNewPlaylist.setOnClickListener {
            val intent = Intent(this, CreatePlaylistActivity::class.java)
            intent.putExtra("trackId", trackId)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        rvPlaylists.layoutManager = LinearLayoutManager(this)

        playlistAdapter = PlaylistSelectionAdapter { selectedPlaylist ->
            Log.d("AdapterClick", "Selected: ${selectedPlaylist.name}")

            playlistViewModel.addTrackToPlaylist(trackId, selectedPlaylist.playlistId, selectedTrack!!)
        }

        rvPlaylists.adapter = playlistAdapter


        playlistViewModel.allPlaylists.observe(this) { playlists ->
            playlistAdapter.submitList(playlists)
        }
    }
}