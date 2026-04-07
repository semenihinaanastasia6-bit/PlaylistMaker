package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView




class PlaylistSelectionAdapter(
    private val onClick: (Playlist) -> Unit
) : ListAdapter<Playlist, PlaylistSelectionAdapter.PlaylistViewHolder>(DIFF_CALLBACK) {
    private var tracks: List<Track> = emptyList()

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Playlist>() {
            override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean =
                oldItem.playlistId == newItem.playlistId

            override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean =
                oldItem == newItem
        }
    }
    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.tv_playlist_name)
    }

    fun submitTracksList(newTracks: List<Track>) {
        this.tracks = newTracks
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }


    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = getItem(position)
        holder.nameTextView.text = playlist.name
        holder.itemView.setOnClickListener { onClick(playlist) }
    }
}

