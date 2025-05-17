package com.projetapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.projetapp.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1) Apply current theme BEFORE super.onCreate
        ThemeHelper.applyTheme(this)

        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2) Play Quiz → MainActivity
        binding.btnPlay.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // 3) Open Settings → SettingsActivity
        binding.btnSetting.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // 4) Exit app
        binding.btnExit.setOnClickListener {
            finishAffinity()
        }
    }
}