package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class TrackAdapter(
    private val tracks: List<Track>,
    private val onItemClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    inner class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val trackName: TextView = view.findViewById(R.id.trackName)
        val artistName: TextView = view.findViewById(R.id.artistName)
        val trackTime: TextView = view.findViewById(R.id.trackTime)
        val artwork: ImageView = view.findViewById(R.id.artwork)

        init {
            view.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(tracks[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.track_item, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]

        holder.trackName.text = track.trackName
        holder.artistName.text = track.artistName

        holder.trackTime.text =
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)

        val artworkUrl = track.artworkUrl100
        if (!artworkUrl.isNullOrEmpty()) {
            Glide.with(holder.artwork.context)
                .load(artworkUrl)
                .placeholder(R.drawable.ic_clear)
                .error(R.drawable.ic_clear)
                .centerCrop()
                .into(holder.artwork)
        } else {
            holder.artwork.setImageResource(R.drawable.ic_clear)
        }
    }

    override fun getItemCount() = tracks.size
}