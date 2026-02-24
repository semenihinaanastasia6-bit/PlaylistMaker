package com.example.playlistmaker

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import android.os.Handler
import android.util.Log



@Suppress("DEPRECATION")
class AudioPlayerActivity : AppCompatActivity() {
    private lateinit var playPauseButton: ImageButton
    private lateinit var trackNameTextView: TextView
    private lateinit var artistNameTextView: TextView
    private lateinit var coverImageView: ImageView
    private lateinit var mediaPlayer: MediaPlayer
    private var isPlaying: Boolean = false
    private var currentTrack: Track? = null
    private lateinit var durationValueText: TextView
    private lateinit var albumValueText: TextView
    private lateinit var yearValueText: TextView
    private lateinit var genreValueText: TextView
    private lateinit var countryValueText: TextView
    private lateinit var favoriteButton: ImageButton
    private lateinit var viewModel: FavoriteTrackViewModel
    private lateinit var backButton: ImageButton
    private lateinit var playbackProgressText: TextView
    private val updateHandler = Handler(Looper.getMainLooper())

    private val updateRunnable: Runnable = object : Runnable {
        override fun run() {
            if (mediaPlayer.isPlaying) {
                val currentPosition = mediaPlayer.currentPosition
                playbackProgressText.text = formatDuration(currentPosition.toLong())
                updateHandler.postDelayed(this, 1000)
            }
        }
    }

    companion object {
        const val EXTRA_TRACK = "EXTRA_TRACK"
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        viewModel = ViewModelProvider(this, FavoriteTrackViewModelFactory(application)).get(FavoriteTrackViewModel::class.java)


        playbackProgressText = findViewById(R.id.playbackProgressText)
        backButton = findViewById(R.id.backButton)
        favoriteButton = findViewById(R.id.favoriteButton)
        playPauseButton = findViewById(R.id.playButton)
        trackNameTextView = findViewById(R.id.trackNameText)
        artistNameTextView = findViewById(R.id.artistNameText)
        coverImageView = findViewById(R.id.coverImage)
        durationValueText = findViewById(R.id.durationValueText)
        albumValueText = findViewById(R.id.albumValueText)
        yearValueText = findViewById(R.id.yearValueText)
        genreValueText = findViewById(R.id.genreValueText)
        countryValueText = findViewById(R.id.countryValueText)


        currentTrack = intent.getParcelableExtra(EXTRA_TRACK, Track::class.java)
        currentTrack?.let { track ->

            trackNameTextView.text = getString(R.string.track_info, track.trackName, track.artistName)
            artistNameTextView.text = track.artistName
            durationValueText.text = formatDuration(track.trackTimeMillis)
            albumValueText.text = track.collectionName ?: "Неизвестно"
            yearValueText.text = track.getYear() ?: "Неизвестно"
            genreValueText.text = track.primaryGenreName ?: "Неизвестно"
            countryValueText.text = track.country ?: "Неизвестно"

            checkIfTrackIsFavorite(track.previewUrl)

            favoriteButton.setOnClickListener {
                toggleFavorite(track)
            }
            track.getCoverArtwork()?.let { highResUrl ->
                Glide.with(this)
                    .load(highResUrl)
                    .placeholder(R.drawable.ic_clear)
                    .error(R.drawable.ic_clear)
                    .centerCrop()
                    .into(coverImageView)
            }
            setUpMediaPlayer(track)
        }

        playPauseButton.setOnClickListener {
            if (isPlaying) {
                pauseTrack()
            } else {
                playTrack()
            }
        }

        backButton.setOnClickListener { onBackPressed() }
    }

    private fun setUpMediaPlayer(track: Track) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(audioAttributes)
            setDataSource(track.previewUrl)
            prepareAsync()
            setOnPreparedListener { playPauseButton.visibility = View.VISIBLE }
            setOnCompletionListener {
                this@AudioPlayerActivity.isPlaying = false
                playPauseButton.setImageResource(R.drawable.ic_play_arrow)
            }
        }
    }

    private fun toggleFavorite(track: Track) {
        lifecycleScope.launch {

            val isFavorite = viewModel.isFavorite(track.previewUrl).value ?: false
            if (isFavorite) {
                viewModel.removeTrackFromFavoritesByUrl(track.previewUrl)

            } else {
                val entity = FavoriteTrackEntity(
                    previewUrl = track.previewUrl,
                    trackName = track.trackName,
                    artistName = track.artistName,
                    trackTimeMillis = track.trackTimeMillis,
                    artworkUrl100 = track.artworkUrl100,
                    collectionName = track.collectionName,
                    releaseDate = track.releaseDate,
                    primaryGenreName = track.primaryGenreName,
                    country = track.country
                )
                viewModel.addTrackToFavorites(entity)

            }


            viewModel.isFavorite(track.previewUrl).observe(this@AudioPlayerActivity) { newFavoriteStatus ->
                updateFavoriteButton(newFavoriteStatus)
            }
        }
    }

    private fun checkIfTrackIsFavorite(previewUrl: String) {
        viewModel.isFavorite(previewUrl).observe(this) { isFavorite ->
            updateFavoriteButton(isFavorite)
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        Log.d("UIUpdate", "Updating button: isFavorite -> $isFavorite")
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.ic_favorite)
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border)
        }

        favoriteButton.requestLayout()
        favoriteButton.invalidate()
    }
    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000).toInt()
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    private fun playTrack() {
        mediaPlayer.start()
        isPlaying = true
        playPauseButton.setImageResource(R.drawable.ic_pause)
        updateHandler.post(updateRunnable)
    }

    private fun pauseTrack() {
        mediaPlayer.pause()
        isPlaying = false
        playPauseButton.setImageResource(R.drawable.ic_play_arrow)
        updateHandler.removeCallbacks(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) pauseTrack()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}