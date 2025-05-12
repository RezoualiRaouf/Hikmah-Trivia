package com.projetapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.projetapp.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: QuizViewModel by viewModels()
    private lateinit var database: DatabaseHelper

    // Broadcast receiver for quiz events
    private val quizEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == QuizEvents.ACTION_LOAD_NEW_QUESTION) {
                // Show reason for question skip
                val reason = when (intent.getStringExtra("reason")) {
                    "call" -> "Incoming call detected! Question skipped."
                    "sms" -> "New SMS received! Question skipped."
                    else -> "Question skipped due to interruption."
                }
                showSkipNotification(reason)
                loadNewQuestion()
            }
        }
    }

    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            viewModel.setPermissionsGranted(true)
        } else {
            Snackbar.make(
                binding.root,
                "Permissions are required for full app functionality",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = (application as QuizApplication).database

        setupUI()

        registerReceiver(
            quizEventReceiver,
            IntentFilter(QuizEvents.ACTION_LOAD_NEW_QUESTION),
            RECEIVER_EXPORTED
        )

        checkPermissions()

        viewModel.currentQuestion.observe(this) { question ->
            displayQuestion(question)
        }

        loadNewQuestion()

        viewModel.score.observe(this) { score ->
            binding.scoreTextView.text = getString(R.string.score, score)
        }
    }

    private fun setupUI() {
        binding.answer1Button.setOnClickListener { checkAnswer(0) }
        binding.answer2Button.setOnClickListener { checkAnswer(1) }
        binding.answer3Button.setOnClickListener { checkAnswer(2) }
        binding.answer4Button.setOnClickListener { checkAnswer(3) }

        binding.nextQuestionButton.setOnClickListener {
            loadNewQuestion()
        }
    }

    private fun showSkipNotification(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("OK") { }
            .setBackgroundTint(ContextCompat.getColor(this, R.color.primary))
            .setTextColor(ContextCompat.getColor(this, R.color.light_text))
            .setActionTextColor(ContextCompat.getColor(this, R.color.light_text))
            .show()
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECEIVE_SMS
        )

        if (permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            viewModel.setPermissionsGranted(true)
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun loadNewQuestion() {
        binding.progressBar.visibility = View.VISIBLE
        binding.questionCardView.alpha = 0.5f

        setAnswerButtonsEnabled(false)

        lifecycleScope.launch {
            delay(300)

            try {
                val newQuestion = database.getRandomQuestion()
                viewModel.setCurrentQuestion(newQuestion)

                setAnswerButtonsEnabled(true)
                binding.progressBar.visibility = View.GONE
                binding.questionCardView.alpha = 1.0f

                binding.resultTextView.text = ""
                binding.nextQuestionButton.visibility = View.GONE

                val fadeIn = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in)
                binding.questionCardView.startAnimation(fadeIn)

            } catch (e: Exception) {
                Snackbar.make(
                    binding.root,
                    "Error loading question: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
                binding.progressBar.visibility = View.GONE
                binding.questionCardView.alpha = 1.0f
            }
        }
    }

    private fun displayQuestion(question: QuizQuestion?) {
        question?.let {
            binding.questionTextView.text = it.questionText
            binding.answer1Button.text = it.options[0]
            binding.answer2Button.text = it.options[1]
            binding.answer3Button.text = it.options[2]
            binding.answer4Button.text = it.options[3]

            resetAnswerButtonStyles()
        }
    }

    private fun checkAnswer(selectedIndex: Int) {
        viewModel.currentQuestion.value?.let { question ->
            setAnswerButtonsEnabled(false)

            val isCorrect = selectedIndex == question.correctOptionIndex

            if (isCorrect) {
                viewModel.incrementScore()
            }

            highlightAnswers(selectedIndex, question.correctOptionIndex)

            binding.resultTextView.text = if (isCorrect) {
                getString(R.string.correct_answer)
            } else {
                getString(R.string.wrong_answer)
            }

            binding.nextQuestionButton.visibility = View.VISIBLE
        }
    }

    private fun highlightAnswers(selectedIndex: Int, correctIndex: Int) {
        val buttons = listOf(
            binding.answer1Button,
            binding.answer2Button,
            binding.answer3Button,
            binding.answer4Button
        )

        buttons[selectedIndex].setBackgroundColor(
            ContextCompat.getColor(
                this,
                if (selectedIndex == correctIndex) R.color.correct_answer else R.color.wrong_answer
            )
        )

        if (selectedIndex != correctIndex) {
            buttons[correctIndex].setBackgroundColor(
                ContextCompat.getColor(this, R.color.correct_answer)
            )
        }
    }

    private fun resetAnswerButtonStyles() {
        val buttons = listOf(
            binding.answer1Button,
            binding.answer2Button,
            binding.answer3Button,
            binding.answer4Button
        )

        buttons.forEach { button ->
            button.setBackgroundColor(
                ContextCompat.getColor(this, R.color.answer_button_background)
            )
        }
    }

    private fun setAnswerButtonsEnabled(enabled: Boolean) {
        binding.answer1Button.isEnabled = enabled
        binding.answer2Button.isEnabled = enabled
        binding.answer3Button.isEnabled = enabled
        binding.answer4Button.isEnabled = enabled
    }

    override fun onPause() {
        super.onPause()
        viewModel.setNeedsNewQuestion(true)
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.needsNewQuestion()) {
            showSkipNotification("Welcome back! Question skipped while you were away.")
            loadNewQuestion()
            viewModel.setNeedsNewQuestion(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(quizEventReceiver)
    }

    companion object {
        private const val RECEIVER_EXPORTED = Context.RECEIVER_EXPORTED
    }
}