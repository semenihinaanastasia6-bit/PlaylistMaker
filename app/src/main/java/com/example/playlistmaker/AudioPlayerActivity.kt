// AudioPlayerActivity.kt
package com.example.playlistmaker

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRACK = "extra_track"
    }

    private lateinit var backButton: ImageButton
    private lateinit var coverImage: ImageView
    private lateinit var trackNameText: TextView
    private lateinit var artistNameText: TextView
    private lateinit var durationValueText: TextView
    private lateinit var albumValueText: TextView
    private lateinit var yearValueText: TextView
    private lateinit var genreValueText: TextView
    private lateinit var countryValueText: TextView
    private lateinit var playbackProgressText: TextView

    private lateinit var addToPlaylistButton: ImageButton
    private lateinit var favoriteButton: ImageButton
    private lateinit var playButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        // Получаем трек из Intent
        val track = intent.getParcelableExtra<Track>(EXTRA_TRACK)
            ?: run {
                finish()
                return
            }

        initViews()
        bindTrack(track)
        setupListeners()
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        coverImage = findViewById(R.id.coverImage)
        trackNameText = findViewById(R.id.trackNameText)
        artistNameText = findViewById(R.id.artistNameText)

        durationValueText = findViewById(R.id.durationValueText)
        albumValueText = findViewById(R.id.albumValueText)
        yearValueText = findViewById(R.id.yearValueText)
        genreValueText = findViewById(R.id.genreValueText)
        countryValueText = findViewById(R.id.countryValueText)

        playbackProgressText = findViewById(R.id.playbackProgressText)

        addToPlaylistButton = findViewById(R.id.addToPlaylistButton)
        favoriteButton = findViewById(R.id.favoriteButton)
        playButton = findViewById(R.id.playButton)
    }

    private fun bindTrack(track: Track) {
        // Название и исполнитель
        trackNameText.text = track.trackName
        artistNameText.text = track.artistName

        // Длительность в формате mm:ss
        val duration = SimpleDateFormat("mm:ss", Locale.getDefault())
            .format(track.trackTimeMillis)
        durationValueText.text = duration

        // Альбом (если есть)
        if (!track.collectionName.isNullOrBlank()) {
            albumValueText.text = track.collectionName
        } else {
            albumValueText.text = "" // или можно скрыть всю строку, если захочешь
        }

        // Год (берём первые 4 символа releaseDate)
        yearValueText.text = track.getYear().orEmpty()

        // Жанр
        genreValueText.text = track.primaryGenreName.orEmpty()

        // Страна
        countryValueText.text = track.country.orEmpty()

        // Прогресс воспроизведения (по заданию пока статичный, логику сделаем позже)
        playbackProgressText.text = "0:00"

        // Обложка 512x512 с заглушкой
        val coverUrl = track.getCoverArtwork()
        if (!coverUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(coverUrl)
                .placeholder(R.drawable.ic_clear) // лучше поставить свой плейсхолдер
                .error(R.drawable.ic_clear)
                .centerCrop()
                .into(coverImage)
        } else {
            coverImage.setImageResource(R.drawable.ic_clear)
        }
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Остальные кнопки пока без логики — по заданию это не требуется
        addToPlaylistButton.setOnClickListener { /* TODO позже */ }
        favoriteButton.setOnClickListener { /* TODO позже */ }
        playButton.setOnClickListener { /* TODO позже */ }
    }
}