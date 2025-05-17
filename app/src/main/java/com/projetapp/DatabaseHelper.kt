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

    // Track questions that have been shown
    private val shownQuestionIds = mutableSetOf<Int>()

    /**
     * Get a random question from the database that hasn't been shown recently
     */
    suspend fun getRandomQuestion(): QuizQuestion = withContext(Dispatchers.IO) {
        val db = readableDatabase
        lateinit var question: QuizQuestion

        // Get total number of questions
        val countQuery = "SELECT COUNT(*) FROM $TABLE_QUESTIONS"
        val countCursor = db.rawQuery(countQuery, null)
        val totalQuestions = if (countCursor.moveToFirst()) countCursor.getInt(0) else 0
        countCursor.close()

        // Reset shown questions if we've shown all questions
        if (shownQuestionIds.size >= totalQuestions) {
            shownQuestionIds.clear()
        }

        // Query to exclude already shown questions
        val excludeIds = if (shownQuestionIds.isEmpty()) {
            ""
        } else {
            " WHERE $COLUMN_ID NOT IN (${shownQuestionIds.joinToString(",")})"
        }

        val query = "SELECT * FROM $TABLE_QUESTIONS$excludeIds ORDER BY RANDOM() LIMIT 1"
        val cursor = db.rawQuery(query, null)

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
            shownQuestionIds.add(id)
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
            // Islamic quiz questions
            QuizQuestion(
                id = 1,
                questionText = "كم عدد أركان الإيمان؟",
                options = listOf("خمسة", "سبعة", "ستة", "ثمانية"),
                correctOptionIndex = 2 // Index is 0-based, so 2 means the third option "ستة"
            ),
            QuizQuestion(
                id = 2,
                questionText = "ما هو أول واجب على العباد؟",
                options = listOf("الصلاة", "الشهادتان", "الصوم", "الزكاة"),
                correctOptionIndex = 1 // "الشهادتان"
            ),
            QuizQuestion(
                id = 3,
                questionText = "كم عدد سور القرآن الكريم؟",
                options = listOf("112", "113", "114", "115"),
                correctOptionIndex = 2 // "114"
            ),
            QuizQuestion(
                id = 4,
                questionText = "ما هي أطول سورة في القرآن؟",
                options = listOf("البقرة", "آل عمران", "النساء", "الأنعام"),
                correctOptionIndex = 0 // "البقرة"
            ),
            QuizQuestion(
                id = 5,
                questionText = "في أي عام وُلد النبي محمد ﷺ؟",
                options = listOf("عام الفيل", "عام الحزن", "عام الهجرة", "عام بدر"),
                correctOptionIndex = 0 // "عام الفيل"
            ),
            QuizQuestion(
                id = 6,
                questionText = "من هو جامع صحيح البخاري؟",
                options = listOf("مسلم بن الحجاج", "أحمد بن حنبل", "محمد بن إسماعيل البخاري", "ابن ماجه"),
                correctOptionIndex = 2 // "محمد بن إسماعيل البخاري"
            ),
            QuizQuestion(
                id = 7,
                questionText = "ما حكم تارك الصلاة جحوداً؟",
                options = listOf("فاسق", "مذنب", "كافر", "عاصي"),
                correctOptionIndex = 2 // "كافر"
            ),
            QuizQuestion(
                id = 8,
                questionText = "ما هي أنواع التوحيد؟",
                options = listOf("توحيد الربوبية فقط", "توحيد الألوهية فقط", "الربوبية والألوهية والأسماء والصفات", "لا يوجد أنواع"),
                correctOptionIndex = 2 // "الربوبية والألوهية والأسماء والصفات"
            ),
            QuizQuestion(
                id = 9,
                questionText = "كم استمرت الدعوة السرية للإسلام؟",
                options = listOf("سنة واحدة", "ثلاث سنوات", "خمس سنوات", "عشر سنوات"),
                correctOptionIndex = 1 // "ثلاث سنوات"
            ),
            QuizQuestion(
                id = 10,
                questionText = "ما هي السورة التي لا تبدأ بالبسملة؟",
                options = listOf("التوبة", "الأنفال", "الفتح", "الجمعة"),
                correctOptionIndex = 0 // "التوبة"
            )
        )
    }
}