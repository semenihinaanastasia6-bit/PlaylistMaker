package com.example.playlistmaker

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import android.widget.ImageView

class MediaLibraryActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter
    private lateinit var viewModel: FavoriteTrackViewModel
    private lateinit var emptyStateImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_media_library)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.action_media

        emptyStateImage = findViewById(R.id.empty_state_image)

        setupRecyclerView()
        setupViewModel()


        db = AppDatabase.getDatabase(applicationContext)
        fetchFavorites()

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    true
                }
                R.id.action_media -> {
                    false
                }
                R.id.action_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            FavoriteTrackViewModelFactory(application)
        ).get(FavoriteTrackViewModel::class.java)
    }

    private fun fetchFavorites() {
        viewModel.getFavoriteTracks().observe(this) { favorites ->
            if (favorites.isNotEmpty()) {
                adapter.submitList(favorites)
                updateEmptyState(false)
            } else {
                adapter.submitList(emptyList())
                updateEmptyState(true)
            }
        }
    }


    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            recyclerView.visibility = View.GONE
            emptyStateImage.visibility = View.VISIBLE
            emptyStateImage.setImageResource(R.drawable.ic_clear)
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateImage.visibility = View.GONE
        }
    }

    private fun openAudioPlayer(track: Track) {
        val intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra(AudioPlayerActivity.EXTRA_TRACK, track)
        startActivity(intent)
    }
}
