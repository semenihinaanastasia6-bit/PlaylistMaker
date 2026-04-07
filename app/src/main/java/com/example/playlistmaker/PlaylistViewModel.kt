package com.example.playlistmaker

import android.app.Application
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import androidx.lifecycle.viewModelScope
import androidx.room.PrimaryKey
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import kotlinx.coroutines.Dispatchers


class PlaylistViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PlaylistRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PlaylistRepository(database.playlistDao(), database.trackDao())
    }

    private val _uiState = MutableLiveData<UiState>(UiState.Loading)
    val uiState: LiveData<UiState> = _uiState
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error


   fun addTrackToPlaylist(trackId: String, playlistId: Long, track: Track) {
    viewModelScope.launch(Dispatchers.IO) {
        val success = repository.addTrackToPlaylist(playlistId, trackId, track)
        withContext(Dispatchers.Main) {
            if (success) {
                _uiState.value = UiState.Success("Трек добавлен в плейлист!")
            } else {
                _uiState.value = UiState.Error("Не удалось добавить трек")
            }
        }
    }
}
    fun getPlaylistTracks(playlistId: Long): LiveData<List<Track>> = repository.getPlaylistTracks(playlistId)


    val allPlaylists: LiveData<List<Playlist>> = repository.allPlaylists

    private val _insertResult = MutableLiveData<Long>()
    val insertResult: LiveData<Long> = _insertResult


    fun insertPlaylist(playlist: Playlist) {
        repository.insert(playlist) { playlistId ->
            _insertResult.postValue(playlistId)
        }
    }
}




class PlaylistAdapter(
    private var playlists: List<Playlist>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_PLAYLIST = 0
    }

    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.tv_playlist_name)
        val descriptionTextView: TextView = itemView.findViewById(R.id.tv_playlist_track_count)
        val coverImageView: ImageView = itemView.findViewById(R.id.iv_playlist_cover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PlaylistViewHolder) {
            val playlist = playlists[position]
            holder.nameTextView.text = playlist.name
            holder.descriptionTextView.text = "${playlist.createdAt}"

            playlist.coverArtworkUrl?.let { url ->
                Glide.with(holder.itemView.context)
                    .load(url)
                    .placeholder(R.drawable.ic_clear)
                    .error(R.drawable.ic_clear)
                    .centerCrop()
                    .into(holder.coverImageView)
            } ?: run {
                holder.coverImageView.setImageResource(R.drawable.ic_clear)
            }


            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, PlaylistTracksActivity::class.java).apply {
                    putExtra("PLAYLIST_ID", playlist.playlistId)
                    putExtra("PLAYLIST_NAME", playlist.name)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = playlists.size

    fun submitList(newPlaylists: List<Playlist>) {
        this.playlists = newPlaylists
        notifyDataSetChanged()
    }
}



