package com.example.playlistmaker

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.util.*
class AudioPlayerActivity : AppCompatActivity() {
    private lateinit var playPauseButton: ImageButton
    private lateinit var trackNameTextView: TextView
    private lateinit var artistNameTextView: TextView
    private lateinit var coverImageView: ImageView
    private lateinit var mediaPlayer: MediaPlayer
    private var isPlaying = false
    private var currentTrack: Track? = null
    private lateinit var durationValueText: TextView
    private lateinit var albumValueText: TextView
    private lateinit var yearValueText: TextView
    private lateinit var genreValueText: TextView
    private lateinit var countryValueText: TextView
    companion object {
        const val EXTRA_TRACK = "EXTRA_TRACK"
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()
        }
        playPauseButton = findViewById(R.id.playButton)
        trackNameTextView = findViewById(R.id.trackNameText)
        artistNameTextView = findViewById(R.id.artistNameText)
        coverImageView = findViewById(R.id.coverImage)
        playPauseButton = findViewById(R.id.playButton)
        trackNameTextView = findViewById(R.id.trackNameText)
        artistNameTextView = findViewById(R.id.artistNameText)
        coverImageView = findViewById(R.id.coverImage)
        durationValueText = findViewById(R.id.durationValueText)
        albumValueText = findViewById(R.id.albumValueText)
        yearValueText = findViewById(R.id.yearValueText)
        genreValueText = findViewById(R.id.genreValueText)
        countryValueText = findViewById(R.id.countryValueText)
        // Use the updated method for retrieving Parcelable
        currentTrack = intent.getParcelableExtra(EXTRA_TRACK, Track::class.java)

        currentTrack?.let {
            trackNameTextView.text = getString(R.string.track_info, it.trackName, it.artistName)
            artistNameTextView.text = it.artistName
            durationValueText.text = formatDuration(it.trackTimeMillis)
            albumValueText.text = it.collectionName ?: "Неизвестно"
            yearValueText.text = it.getYear() ?: "Неизвестно"
            genreValueText.text = it.primaryGenreName ?: "Неизвестно"
            countryValueText.text = it.country ?: "Неизвестно"

            // Load cover image using Glide
            it.getCoverArtwork()?.let { highResUrl ->
                Glide.with(this)
                    .load(highResUrl)
                    .placeholder(R.drawable.ic_clear)
                    .error(R.drawable.ic_clear)
                    .centerCrop()
                    .into(coverImageView)
            }


            // Prepare MediaPlayer with AudioAttributes
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
                setDataSource(it.previewUrl)
                prepareAsync()
                setOnPreparedListener { playPauseButton.visibility = View.VISIBLE }
                setOnCompletionListener {
                    this@AudioPlayerActivity.isPlaying = false
                    playPauseButton.setImageResource(R.drawable.ic_play_arrow)
                }
            }
        }

        playPauseButton.setOnClickListener {
            if (isPlaying) {
                pauseTrack()
            } else {
                playTrack()
            }
        }
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
    }

    private fun pauseTrack() {
        mediaPlayer.pause()
        isPlaying = false
        playPauseButton.setImageResource(R.drawable.ic_play_arrow)
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            pauseTrack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}