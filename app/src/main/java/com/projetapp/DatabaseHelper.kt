package com.projetapp


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper class to manage the SQLite database for quiz questions
 */
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "DatabaseHelper"

        // Database metadata
        private const val DATABASE_NAME = "QuizDatabase.db"
        private const val DATABASE_VERSION = 1

        // Tables and columns
        private const val TABLE_QUESTIONS = "questions"
        private const val COLUMN_ID = "id"
        private const val COLUMN_QUESTION_TEXT = "question_text"
        private const val COLUMN_OPTION_1 = "option_1"
        private const val COLUMN_OPTION_2 = "option_2"
        private const val COLUMN_OPTION_3 = "option_3"
        private const val COLUMN_OPTION_4 = "option_4"
        private const val COLUMN_CORRECT_OPTION = "correct_option"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create the questions table
        val createTableQuery = """
            CREATE TABLE $TABLE_QUESTIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_QUESTION_TEXT TEXT NOT NULL,
                $COLUMN_OPTION_1 TEXT NOT NULL,
                $COLUMN_OPTION_2 TEXT NOT NULL,
                $COLUMN_OPTION_3 TEXT NOT NULL,
                $COLUMN_OPTION_4 TEXT NOT NULL,
                $COLUMN_CORRECT_OPTION INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createTableQuery)

        // Insert sample questions
        insertSampleQuestions(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // On upgrade, drop the existing table and recreate it
        db.execSQL("DROP TABLE IF EXISTS $TABLE_QUESTIONS")
        onCreate(db)
    }

    private fun insertSampleQuestions(db: SQLiteDatabase) {
        val sampleQuestions = getSampleQuestions()
        for (question in sampleQuestions) {
            val values = ContentValues().apply {
                put(COLUMN_QUESTION_TEXT, question.questionText)
                put(COLUMN_OPTION_1, question.options[0])
                put(COLUMN_OPTION_2, question.options[1])
                put(COLUMN_OPTION_3, question.options[2])
                put(COLUMN_OPTION_4, question.options[3])
                put(COLUMN_CORRECT_OPTION, question.correctOptionIndex)
            }
            db.insert(TABLE_QUESTIONS, null, values)
        }
    }

    /**
     * Ensure we have questions in the database, adding sample questions if needed
     */
    fun ensureQuestionsExists() {
        val questionsCount = getQuestionsCount()
        if (questionsCount == 0) {
            val db = writableDatabase
            insertSampleQuestions(db)
            db.close()
        }
    }

    /**
     * Get a count of questions in the database
     */
    private fun getQuestionsCount(): Int {
        val db = readableDatabase
        val countQuery = "SELECT COUNT(*) FROM $TABLE_QUESTIONS"
        val cursor = db.rawQuery(countQuery, null)
        var count = 0

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()

        return count
    }

    /**
     * Get a random question from the database
     */
    suspend fun getRandomQuestion(): QuizQuestion = withContext(Dispatchers.IO) {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_QUESTIONS ORDER BY RANDOM() LIMIT 1"
        val cursor = db.rawQuery(query, null)
        lateinit var question: QuizQuestion

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val questionText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QUESTION_TEXT))
            val options = listOf(
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPTION_1)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPTION_2)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPTION_3)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPTION_4))
            )
            val correctOptionIndex = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CORRECT_OPTION))

            question = QuizQuestion(id, questionText, options, correctOptionIndex)
        } else {
            // If no questions found, create a default one (this should not happen if setup correctly)
            Log.e(TAG, "No questions found in database")
            question = getSampleQuestions().first()
        }

        cursor.close()
        db.close()

        return@withContext question
    }

    /**
     * Add a new question to the database
     */
    suspend fun addQuestion(question: QuizQuestion): Long = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_QUESTION_TEXT, question.questionText)
            put(COLUMN_OPTION_1, question.options[0])
            put(COLUMN_OPTION_2, question.options[1])
            put(COLUMN_OPTION_3, question.options[2])
            put(COLUMN_OPTION_4, question.options[3])
            put(COLUMN_CORRECT_OPTION, question.correctOptionIndex)
        }

        val id = db.insert(TABLE_QUESTIONS, null, values)
        db.close()

        return@withContext id
    }

    /**
     * Sample questions for initial database setup
     */
    private fun getSampleQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                id = 1,
                questionText = "What is the capital of France?",
                options = listOf("London", "Paris", "Berlin", "Madrid"),
                correctOptionIndex = 1
            ),
            QuizQuestion(
                id = 2,
                questionText = "Which planet is known as the Red Planet?",
                options = listOf("Venus", "Mars", "Jupiter", "Saturn"),
                correctOptionIndex = 1
            ),
            QuizQuestion(
                id = 3,
                questionText = "What is the largest mammal on Earth?",
                options = listOf("Elephant", "Giraffe", "Blue Whale", "Hippopotamus"),
                correctOptionIndex = 2
            ),
            QuizQuestion(
                id = 4,
                questionText = "Who wrote 'Romeo and Juliet'?",
                options = listOf("Charles Dickens", "William Shakespeare", "Jane Austen", "Mark Twain"),
                correctOptionIndex = 1
            ),
            QuizQuestion(
                id = 5,
                questionText = "What is the chemical symbol for gold?",
                options = listOf("Go", "Gd", "Au", "Ag"),
                correctOptionIndex = 2
            ),
            QuizQuestion(
                id = 6,
                questionText = "What is the main component of the Earth's atmosphere?",
                options = listOf("Oxygen", "Carbon Dioxide", "Nitrogen", "Hydrogen"),
                correctOptionIndex = 2
            ),
            QuizQuestion(
                id = 7,
                questionText = "Which country is home to the kangaroo?",
                options = listOf("New Zealand", "South Africa", "Australia", "Brazil"),
                correctOptionIndex = 2
            ),
            QuizQuestion(
                id = 8,
                questionText = "How many sides does a hexagon have?",
                options = listOf("5", "6", "7", "8"),
                correctOptionIndex = 1
            ),
            QuizQuestion(
                id = 9,
                questionText = "What is the largest organ in the human body?",
                options = listOf("Brain", "Liver", "Heart", "Skin"),
                correctOptionIndex = 3
            ),
            QuizQuestion(
                id = 10,
                questionText = "Which is the longest river in the world?",
                options = listOf("Amazon", "Nile", "Mississippi", "Yangtze"),
                correctOptionIndex = 1
            )
        )
    }
}