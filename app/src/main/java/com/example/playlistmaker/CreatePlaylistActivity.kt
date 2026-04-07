package com.example.playlistmaker

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView

class CreatePlaylistActivity : AppCompatActivity() {
    private lateinit var btnCreate: Button
    private lateinit var playlistNameEditText: EditText
    private lateinit var playlistDescriptionEditText: EditText
    private lateinit var playlistCoverImageView: ImageView
    private var coverImageUri: Uri? = null
    private lateinit var viewModel: PlaylistViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_playlist)

        btnCreate = findViewById(R.id.btnCreate)
        playlistNameEditText = findViewById(R.id.playlistName)
        playlistDescriptionEditText = findViewById(R.id.playlistDescription)
        playlistCoverImageView = findViewById(R.id.playlistCover)

        viewModel = ViewModelProvider(this).get(PlaylistViewModel::class.java)
        setupListeners()
    }

    private fun setupListeners() {
        playlistNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnCreate.isEnabled = !s.isNullOrBlank()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        playlistCoverImageView.setOnClickListener {
            pickImageFromGallery()
        }

        btnCreate.setOnClickListener {
            createPlaylist()
        }

        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            promptUnsavedChanges()
        }

        playlistCoverImageView.setImageResource(R.drawable.ic_add_image)
    }


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                coverImageUri = uri
                playlistCoverImageView.setImageURI(uri)
            }
        }
    }

    private fun createPlaylist() {
        val name = playlistNameEditText.text.toString().trim()
        val description = playlistDescriptionEditText.text.toString()

        if (name.isBlank()) {
            Toast.makeText(this, "Введите название плейлиста", Toast.LENGTH_SHORT).show()
            return
        }

        val coverUrl = coverImageUri?.toString() ?: ""
        val newPlaylist = Playlist(
            name = name,
            description = description,
            coverArtworkUrl = coverUrl
        )

        viewModel.insertPlaylist(newPlaylist)

        viewModel.insertResult.observe(this, Observer { playlistId ->
            if (playlistId != null && playlistId > 0) {
                Toast.makeText(
                    this,
                    "Плейлист \"$name\" создан (ID: $playlistId)",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        })
    }



    private fun promptUnsavedChanges() {
        val hasUnsavedChanges = playlistNameEditText.text.isNotEmpty() ||
                playlistDescriptionEditText.text.isNotEmpty() ||
                coverImageUri != null

        if (hasUnsavedChanges) {
            AlertDialog.Builder(this)
                .setTitle("Завершить создание плейлиста?")
                .setMessage("Все несохраненные данные будут потеряны")
                .setPositiveButton("Завершить") { _, _ -> onBackPressed() }
                .setNegativeButton("Отмена", null)
                .show()
        } else {
            onBackPressed()
        }
    }
    companion object {
        private const val REQUEST_IMAGE_PICK = 1
    }
}
