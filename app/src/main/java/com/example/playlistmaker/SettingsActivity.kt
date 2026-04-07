package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit


class SettingsActivity : ComponentActivity() {
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener { finish() }
        val shareButton = findViewById<ImageView>(R.id.share_button)

        val themeSwitch = findViewById<Switch>(R.id.theme_switch)

        val isDarkTheme = getSharedPreferences("themePrefs", MODE_PRIVATE)
            .getBoolean("darkTheme", false)

        themeSwitch.isChecked = getSharedPreferences("themePrefs", MODE_PRIVATE)
            .getBoolean("darkTheme", false)

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            getSharedPreferences("themePrefs", MODE_PRIVATE).edit {
                putBoolean("darkTheme", isChecked)
            }
            recreate()
        }
        shareButton.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
        val supportButton = findViewById<ImageView>(R.id.support_button)


        supportButton.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("semenihinaanastasia6@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body))
            }

            startActivity(emailIntent)
        }
        val agreementButton = findViewById<LinearLayout>(R.id.agreement_button)


        agreementButton.setOnClickListener {
            val url = getString(R.string.agreement_url)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }
}


