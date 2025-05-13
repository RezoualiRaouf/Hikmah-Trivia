package com.projetapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing a quiz question
 *
 * @param id Unique identifier for the question
 * @param questionText The text of the question to display
 * @param options List of possible answers (always 4 options)
 * @param correctOptionIndex Index of the correct answer in the options list (0-3)
 */
@Parcelize
data class QuizQuestion(
    val id: Int,
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int
) : Parcelable {
    // Validate the question data
    init {
        require(options.size == 4) { "A question must have exactly 4 options" }
        require(correctOptionIndex in 0..3) { "Correct option index must be between 0 and 3" }
    }
}

