package com.blueboss.terbility.ui

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("TerbilityPrefs", Context.MODE_PRIVATE)

        findViewById<Button>(R.id.color_pink).setOnClickListener { saveColor(prefs, "#FF1493") }
        findViewById<Button>(R.id.color_green).setOnClickListener { saveColor(prefs, "#00FF00") }
        findViewById<Button>(R.id.color_cyan).setOnClickListener { saveColor(prefs, "#00FFFF") }
        findViewById<Button>(R.id.color_orange).setOnClickListener { saveColor(prefs, "#FFA500") }
        findViewById<Button>(R.id.color_purple).setOnClickListener { saveColor(prefs, "#BF40BF") }

        findViewById<Button>(R.id.theme_dark).setOnClickListener {
            prefs.edit().putString("theme_mode", "dark").apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            recreate()
        }
        findViewById<Button>(R.id.theme_light).setOnClickListener {
            prefs.edit().putString("theme_mode", "light").apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            recreate()
        }
    }

    private fun saveColor(prefs: android.content.SharedPreferences, color: String) {
        prefs.edit().putString("accent_color", color).apply()
        finish()
    }
}
