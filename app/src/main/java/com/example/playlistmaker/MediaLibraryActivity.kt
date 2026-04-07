package com.example.playlistmaker

import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import android.widget.ImageView
import android.widget.TextView

class MediaLibraryActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter
    private lateinit var viewModel: FavoriteTrackViewModel
    private lateinit var emptyStateImage: ImageView
    private lateinit var rvPlaylists: RecyclerView
    private lateinit var imgNoPlaylists: ImageView
    private lateinit var tvFavorites: TextView
    private lateinit var tvPlaylists: TextView
    private lateinit var rvTracks: RecyclerView
    private lateinit var btnNewPlaylist: Button
    private lateinit var textNoPlaylists: TextView
    private lateinit var playlistViewModel: PlaylistViewModel
    private lateinit var emptyStateText: TextView
    private var playlistAdapter: PlaylistAdapter? = null
    private var isFavoritesSelected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_library)

        tvFavorites = findViewById(R.id.tv_favorites)
        tvPlaylists = findViewById(R.id.action_playlists)
        rvTracks = findViewById(R.id.rv_tracks)
        btnNewPlaylist = findViewById(R.id.btn_new_playlist)
        emptyStateImage = findViewById(R.id.empty_state_image)
        rvPlaylists = findViewById(R.id.playlists_list)
        imgNoPlaylists = findViewById(R.id.img_no_playlists)
        textNoPlaylists = findViewById(R.id.text_error)
        emptyStateText = findViewById(R.id.empty_state_text)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.action_media

        setupViewModel()
        setupPlaylistViewModel()
        db = AppDatabase.getDatabase(applicationContext)


        setupRecyclerView()
        setupRecyclerViewForPlaylists()
        hideAllViews()
        fetchFavorites()

        updateTabStyles()

        tvFavorites.setOnClickListener {
            isFavoritesSelected = true
            showFavoritesSection()
            updateTabStyles()
        }
        tvPlaylists.setOnClickListener {
            isFavoritesSelected = false
            showPlaylistsSection()
            updateTabStyles()
        }


        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    true
                }
                R.id.action_media -> {
                    isFavoritesSelected = true
                    showFavoritesSection()
                    updateTabStyles()
                    true
                }
                R.id.action_playlists -> {
                    isFavoritesSelected = false
                    showPlaylistsSection()
                    updateTabStyles()
                    true
                }
                R.id.action_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }


        btnNewPlaylist.setOnClickListener {
            val intent = Intent(this, CreatePlaylistActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun hideAllViews() {
        rvTracks.visibility = View.GONE
        rvPlaylists.visibility = View.GONE
        emptyStateImage.visibility = View.GONE
        emptyStateText.visibility = View.GONE
        imgNoPlaylists.visibility = View.GONE
        textNoPlaylists.visibility = View.GONE
        btnNewPlaylist.visibility = View.GONE
    }

    private fun updateTabStyles() {
        tvFavorites.setTypeface(null, if (isFavoritesSelected) Typeface.BOLD else Typeface.NORMAL)
        tvPlaylists.setTypeface(null, if (!isFavoritesSelected) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, FavoriteTrackViewModelFactory(application))
            .get(FavoriteTrackViewModel::class.java)
    }

    private fun setupPlaylistViewModel() {
        playlistViewModel = ViewModelProvider(this)[PlaylistViewModel::class.java]
        playlistViewModel.allPlaylists.observe(this) { playlists ->
            if (!isFavoritesSelected) {
                showPlaylistsSection()
            }
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rv_tracks)
        adapter = TrackAdapter(emptyList()) { track ->
            openAudioPlayer(track)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupRecyclerViewForPlaylists() {
        rvPlaylists.layoutManager = LinearLayoutManager(this)
        playlistAdapter = PlaylistAdapter(emptyList())
        rvPlaylists.adapter = playlistAdapter
    }

    private fun fetchFavorites() {
        viewModel.getFavoriteTracks().observe(this) { favorites ->
            Log.d("MediaLibrary", "Получено избранных треков: ${favorites.size}")


            adapter.submitList(favorites)

            if (isFavoritesSelected) {
                showFavoritesSection()
            }
        }
    }

    private fun showFavoritesSection() {
        isFavoritesSelected = true


        rvPlaylists.visibility = View.GONE
        imgNoPlaylists.visibility = View.GONE
        textNoPlaylists.visibility = View.GONE
        btnNewPlaylist.visibility = View.GONE

        rvTracks.visibility = View.VISIBLE

        updateEmptyState(adapter.getCurrentList().isEmpty())
    }

    private fun showPlaylistsSection() {
        isFavoritesSelected = false


        rvTracks.visibility = View.GONE
        emptyStateImage.visibility = View.GONE
        emptyStateText.visibility = View.GONE


        rvPlaylists.visibility = View.VISIBLE
        btnNewPlaylist.visibility = View.VISIBLE

        val playlists = playlistViewModel.allPlaylists.value.orEmpty()
        playlistAdapter?.submitList(playlists)

        if (playlists.isEmpty()) {
            imgNoPlaylists.visibility = View.VISIBLE
            textNoPlaylists.visibility = View.VISIBLE
            textNoPlaylists.text = "Вы не создали ни одного плейлиста"
        } else {
            imgNoPlaylists.visibility = View.GONE
            textNoPlaylists.visibility = View.GONE
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            rvTracks.visibility = View.GONE
            emptyStateImage.visibility = View.VISIBLE
            emptyStateText.visibility = View.VISIBLE
            emptyStateImage.setImageResource(R.drawable.ic_clear)
            emptyStateText.text = "У вас нет избранных треков"
            Log.d("MediaLibrary", "Показано пустое состояние")
        } else {
            rvTracks.visibility = View.VISIBLE
            emptyStateImage.visibility = View.GONE
            emptyStateText.visibility = View.GONE
            Log.d("MediaLibrary", "Показаны треки")
        }
    }

    private fun openAudioPlayer(track: Track) {
        val intent = Intent(this, AudioPlayerActivity::class.java).apply {
            putExtra(AudioPlayerActivity.EXTRA_TRACK, track)
        }
        startActivity(intent)
    }
}