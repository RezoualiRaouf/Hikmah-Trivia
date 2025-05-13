package com.projetapp

import android.app.Application

class QuizApplication : Application() {

    // Initialize database at application start
    val database by lazy {
        DatabaseHelper(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Preload questions if needed
        database.ensureQuestionsExists()
    }
}