package com.blueboss.terbility

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("TerbilityPrefs", Context.MODE_PRIVATE)

        findViewById<Button>(R.id.color_pink).setOnClickListener {
            prefs.edit().putString("accent_color", "#FF1493").apply()
            finish()
        }
        findViewById<Button>(R.id.color_green).setOnClickListener {
            prefs.edit().putString("accent_color", "#00FF00").apply()
            finish()
        }
        findViewById<Button>(R.id.color_cyan).setOnClickListener {
            prefs.edit().putString("accent_color", "#00FFFF").apply()
            finish()
        }
        findViewById<Button>(R.id.color_orange).setOnClickListener {
            prefs.edit().putString("accent_color", "#FFA500").apply()
            finish()
        }
        findViewById<Button>(R.id.color_purple).setOnClickListener {
            prefs.edit().putString("accent_color", "#BF40BF").apply()
            finish()
        }
    }
}
