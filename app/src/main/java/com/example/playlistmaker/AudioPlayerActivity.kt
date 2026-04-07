package com.example.playlistmaker

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.Locale

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
    private val updateHandler = Handler()
    private var currentIsFavorite = false
    private lateinit var addToPlaylistButton: ImageButton
    private lateinit var trackRepository: TrackRepository
    private lateinit var playlistRepository: PlaylistRepository

    private val updateRunnable: Runnable = object : Runnable {
        override fun run() {
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                val currentPosition = mediaPlayer.currentPosition
                playbackProgressText.text = formatDuration(currentPosition.toLong())
                updateHandler.postDelayed(this, 1000)
            }
        }
    }

    companion object {
        const val EXTRA_TRACK = "EXTRA_TRACK"
        const val REQUEST_CODE_ADD_TO_PLAYLIST = 1001
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        viewModel = ViewModelProvider(this, FavoriteTrackViewModelFactory(application))
            .get(FavoriteTrackViewModel::class.java)

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

        trackRepository = TrackRepository(AppDatabase.getDatabase(application).trackDao())
        playlistRepository = PlaylistRepository(
            AppDatabase.getDatabase(application).playlistDao(),
            AppDatabase.getDatabase(application).trackDao()
        )

        currentTrack = intent.getParcelableExtra(EXTRA_TRACK, Track::class.java)
        currentTrack?.let { track ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {

                    if (!trackRepository.trackExists(track.trackId)) {
                        val rowId = trackRepository.insertTrack(track)
                        if (rowId <= 0L) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@AudioPlayerActivity, "Трек уже сохранён или ошибка", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        trackNameTextView.text = getString(R.string.track_info, track.trackName, track.artistName)
                        artistNameTextView.text = track.artistName
                        durationValueText.text = formatDuration(track.trackTimeMillis)
                        albumValueText.text = track.collectionName ?: "Неизвестно"
                        yearValueText.text = track.getYear() ?: "Неизвестно"
                        genreValueText.text = track.primaryGenreName ?: "Неизвестно"
                        countryValueText.text = track.country ?: "Неизвестно"

                        checkIfTrackIsFavorite(track.previewUrl)
                        favoriteButton.setOnClickListener { toggleFavorite(track) }

                        loadCoverImage(track)
                        setUpMediaPlayer(track)
                    }
                } catch (e: Exception) {
                    Log.e("TrackSave", "Ошибка сохранения: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AudioPlayerActivity, "Ошибка при сохранении трека: ${e.message}", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }

        playPauseButton.setOnClickListener {
            if (isPlaying) pauseTrack() else playTrack()
        }
        backButton.setOnClickListener { onBackPressed() }

        addToPlaylistButton = findViewById(R.id.addToPlaylistButton)
        addToPlaylistButton.setOnClickListener {
            val intent = Intent(this, PlaylistSelectionActivity::class.java)
            currentTrack?.let { track ->
                intent.putExtra("trackId", track.trackId)
                intent.putExtra("track", track)
                startActivityForResult(intent, REQUEST_CODE_ADD_TO_PLAYLIST)
            } ?: Toast.makeText(this, "Ошибка: трек не определен", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCoverImage(track: Track) {
        track.getCoverArtwork()?.takeIf { it.isNotEmpty() }?.let { highResUrl ->
            Glide.with(this)
                .asBitmap()
                .load(highResUrl)
                .placeholder(R.drawable.ic_clear)
                .error(R.drawable.ic_clear)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                        Log.w("ImageLoad", "Glide failed: ${e?.message}")
                        return false
                    }
                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: com.bumptech.glide.load.DataSource?, isFirstResource: Boolean): Boolean = false
                })
                .into(coverImageView)
        } ?: run { coverImageView.setImageResource(R.drawable.ic_clear) }
    }

    private fun setUpMediaPlayer(track: Track) {
        if (!::mediaPlayer.isInitialized) {
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
    }

    private fun toggleFavorite(track: Track) {
        lifecycleScope.launch {
            if (currentIsFavorite) {
                viewModel.removeTrackFromFavoritesByUrl(track.previewUrl)
            } else {
                val entity = FavoriteTrackEntity(
                    trackId = track.trackId,
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
        }
    }

    private fun checkIfTrackIsFavorite(previewUrl: String) {
        viewModel.isFavorite(previewUrl).observe(this) { isFavorite ->
            currentIsFavorite = isFavorite
            updateFavoriteButton(isFavorite)
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        Log.d("UIUpdate", "Updating button: isFavorite -> $isFavorite")
        favoriteButton.setImageResource(if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
        favoriteButton.requestLayout()
        favoriteButton.invalidate()
    }

    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000).toInt()
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, secs)
    }

    private fun playTrack() {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.start()
            isPlaying = true
            playPauseButton.setImageResource(R.drawable.ic_pause)
            updateHandler.post(updateRunnable)
        }
    }

    private fun pauseTrack() {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.pause()
            isPlaying = false
            playPauseButton.setImageResource(R.drawable.ic_play_arrow)
            updateHandler.removeCallbacks(updateRunnable)
        }
    }

    override fun onPause() {
        super.onPause()
        if (::mediaPlayer.isInitialized && isPlaying) pauseTrack()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) mediaPlayer.release()
        updateHandler.removeCallbacks(updateRunnable)
    }
}