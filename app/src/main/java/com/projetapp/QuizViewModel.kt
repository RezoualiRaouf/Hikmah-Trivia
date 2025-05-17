package com.projetapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel that holds the state of the quiz
 */
class QuizViewModel : ViewModel() {

    // Current question being displayed
    private val _currentQuestion = MutableLiveData<QuizQuestion>()
    val currentQuestion: LiveData<QuizQuestion> = _currentQuestion

    // User's score
    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    // Flag to track if permissions are granted
    private val _permissionsGranted = MutableLiveData(false)
    val permissionsGranted: LiveData<Boolean> = _permissionsGranted

    // Flag to track if we need to load a new question on resume
    private var _needsNewQuestion = false

    /**
     * Set the current question to display
     */
    fun setCurrentQuestion(question: QuizQuestion) {
        _currentQuestion.value = question
    }

    /**
     * Increment the score when the user answers correctly
     */
    fun incrementScore() {
        _score.value = (_score.value ?: 0) + 1
    }

    /**
     * Set whether the required permissions are granted
     */
    fun setPermissionsGranted(granted: Boolean) {
        _permissionsGranted.value = granted
    }

    /**
     * Set whether a new question needs to be loaded on resume
     */
    fun setNeedsNewQuestion(needs: Boolean) {
        _needsNewQuestion = needs
    }

    /**
     * Check if a new question needs to be loaded on resume
     */
    fun needsNewQuestion(): Boolean {
        return _needsNewQuestion
    }

    fun resetScore() {
        _score.value = 0

    }
}