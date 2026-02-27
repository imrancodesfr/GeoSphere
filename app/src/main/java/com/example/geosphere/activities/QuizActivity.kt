package com.example.geosphere.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.geosphere.R
import com.example.geosphere.databinding.ActivityQuizBinding
import com.example.geosphere.models.Question
import com.example.geosphere.utils.Constants
import com.example.geosphere.utils.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class QuizActivity : BaseActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseHelper: FirebaseHelper

    private var categoryId: String = ""
    private var categoryName: String = ""
    private var questions: MutableList<Question> = mutableListOf()
    private var currentQuestionIndex = 0
    private var score = 0
    private var selectedOptionIndex = -1
    private var correctAnswers = 0
    private var totalQuestions = 0

    private lateinit var timer: CountDownTimer
    private var timeLeft = Constants.TIME_PER_QUESTION * 1000L
    private var isTimerRunning = false
    // Tracks whether answer has already been submitted for the current question
    private var answerSubmitted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firebaseHelper = FirebaseHelper(this)

        // Get intent data
        categoryId = intent.getStringExtra("category_id") ?: "world"
        categoryName = intent.getStringExtra("category_name") ?: "World"

        setupToolbar()
        loadQuestions()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = categoryName
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupClickListeners() {
        binding.btnOption1.setOnClickListener { onOptionSelected(0) }
        binding.btnOption2.setOnClickListener { onOptionSelected(1) }
        binding.btnOption3.setOnClickListener { onOptionSelected(2) }
        binding.btnOption4.setOnClickListener { onOptionSelected(3) }

        binding.btnSubmit.setOnClickListener {
            submitAnswer()
        }

        binding.btnNext.setOnClickListener {
            loadNextQuestion()
        }
    }

    private fun loadQuestions() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val result = firebaseHelper.getQuestionsByCategory(categoryId)
                if (result.isSuccess) {
                    questions = result.getOrDefault(emptyList()).toMutableList()
                    totalQuestions = questions.size

                    if (questions.isEmpty()) {
                        Toast.makeText(this@QuizActivity,
                            getString(R.string.no_questions_available), Toast.LENGTH_SHORT).show()
                        finish()
                        return@launch
                    }

                    // Shuffle questions
                    questions.shuffle()

                    // Take only DEFAULT_QUESTIONS_PER_QUIZ or all if less
                    if (questions.size > Constants.DEFAULT_QUESTIONS_PER_QUIZ) {
                        questions = questions.subList(0, Constants.DEFAULT_QUESTIONS_PER_QUIZ).toMutableList()
                    }

                    totalQuestions = questions.size
                    displayQuestion()
                } else {
                    Toast.makeText(this@QuizActivity,
                        getString(R.string.error_loading_questions), Toast.LENGTH_SHORT).show()
                    finish()
                }
                showLoading(false)
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@QuizActivity,
                    getString(R.string.error_format, e.message), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayQuestion() {
        if (currentQuestionIndex >= questions.size) {
            finishQuiz()
            return
        }

        val question = questions[currentQuestionIndex]

        // Update UI with string resources
        binding.tvQuestionNumber.text = getString(R.string.question_format,
            currentQuestionIndex + 1, totalQuestions)
        binding.tvQuestionText.text = question.questionText
        binding.btnOption1.text = question.options.getOrElse(0) { getString(R.string.option_a) }
        binding.btnOption2.text = question.options.getOrElse(1) { getString(R.string.option_b) }
        binding.btnOption3.text = question.options.getOrElse(2) { getString(R.string.option_c) }
        binding.btnOption4.text = question.options.getOrElse(3) { getString(R.string.option_d) }

        // Reset UI state
        selectedOptionIndex = -1
        answerSubmitted = false
        resetOptionStyles()

        // Re-enable all option buttons for the new question
        setOptionsEnabled(true)

        binding.btnSubmit.isEnabled = true
        binding.btnSubmit.visibility = View.VISIBLE
        binding.btnNext.visibility = View.GONE

        // Reset timer text color
        binding.tvTimer.setTextColor(resources.getColor(android.R.color.white, null))

        // Update progress
        updateProgress()

        // Start timer
        startTimer()
    }

    private fun onOptionSelected(index: Int) {
        // Prevent changing selection after submitting
        if (answerSubmitted) return
        selectedOptionIndex = index
        updateOptionStyles()
    }

    private fun updateOptionStyles() {
        resetOptionStyles()

        when (selectedOptionIndex) {
            0 -> binding.btnOption1.setBackgroundResource(R.drawable.bg_option_selected)
            1 -> binding.btnOption2.setBackgroundResource(R.drawable.bg_option_selected)
            2 -> binding.btnOption3.setBackgroundResource(R.drawable.bg_option_selected)
            3 -> binding.btnOption4.setBackgroundResource(R.drawable.bg_option_selected)
        }
    }

    private fun resetOptionStyles() {
        binding.btnOption1.setBackgroundResource(R.drawable.bg_option_normal)
        binding.btnOption2.setBackgroundResource(R.drawable.bg_option_normal)
        binding.btnOption3.setBackgroundResource(R.drawable.bg_option_normal)
        binding.btnOption4.setBackgroundResource(R.drawable.bg_option_normal)
    }

    private fun setOptionsEnabled(enabled: Boolean) {
        binding.btnOption1.isEnabled = enabled
        binding.btnOption2.isEnabled = enabled
        binding.btnOption3.isEnabled = enabled
        binding.btnOption4.isEnabled = enabled
    }

    private fun submitAnswer() {
        if (answerSubmitted) return // Prevent double-submit

        if (selectedOptionIndex == -1) {
            Toast.makeText(this, getString(R.string.please_select_answer), Toast.LENGTH_SHORT).show()
            return
        }

        answerSubmitted = true
        stopTimer()
        setOptionsEnabled(false) // Lock options after submit

        val currentQuestion = questions[currentQuestionIndex]
        val isCorrect = selectedOptionIndex == currentQuestion.correctOptionIndex

        if (isCorrect) {
            correctAnswers++
            score += currentQuestion.points
            highlightCorrectAnswer()
            animateCorrectAnswer()
        } else {
            highlightWrongAnswer()
            animateWrongAnswer()
        }

        // Disable submit button, show next button
        binding.btnSubmit.isEnabled = false
        binding.btnNext.visibility = View.VISIBLE

        // Animate next button
        binding.btnNext.alpha = 0f
        binding.btnNext.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
    }

    /** Called when timer runs out — marks question as wrong and advances */
    private fun onTimeUp() {
        if (answerSubmitted) return // Already answered manually

        answerSubmitted = true
        setOptionsEnabled(false)

        // Highlight correct answer in green so user can learn
        highlightCorrectAnswer()

        // Disable submit, show next
        binding.btnSubmit.isEnabled = false
        binding.btnNext.visibility = View.VISIBLE

        binding.btnNext.alpha = 0f
        binding.btnNext.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
    }

    private fun highlightCorrectAnswer() {
        val correctIndex = questions[currentQuestionIndex].correctOptionIndex
        when (correctIndex) {
            0 -> binding.btnOption1.setBackgroundResource(R.drawable.bg_option_correct)
            1 -> binding.btnOption2.setBackgroundResource(R.drawable.bg_option_correct)
            2 -> binding.btnOption3.setBackgroundResource(R.drawable.bg_option_correct)
            3 -> binding.btnOption4.setBackgroundResource(R.drawable.bg_option_correct)
        }
    }

    private fun highlightWrongAnswer() {
        val correctIndex = questions[currentQuestionIndex].correctOptionIndex
        when (correctIndex) {
            0 -> binding.btnOption1.setBackgroundResource(R.drawable.bg_option_correct)
            1 -> binding.btnOption2.setBackgroundResource(R.drawable.bg_option_correct)
            2 -> binding.btnOption3.setBackgroundResource(R.drawable.bg_option_correct)
            3 -> binding.btnOption4.setBackgroundResource(R.drawable.bg_option_correct)
        }

        when (selectedOptionIndex) {
            0 -> binding.btnOption1.setBackgroundResource(R.drawable.bg_option_wrong)
            1 -> binding.btnOption2.setBackgroundResource(R.drawable.bg_option_wrong)
            2 -> binding.btnOption3.setBackgroundResource(R.drawable.bg_option_wrong)
            3 -> binding.btnOption4.setBackgroundResource(R.drawable.bg_option_wrong)
        }
    }

    private fun animateCorrectAnswer() {
        binding.ivCorrect.visibility = View.VISIBLE
        binding.ivCorrect.alpha = 0f
        binding.ivCorrect.animate()
            .alpha(1f)
            .scaleX(1.5f)
            .scaleY(1.5f)
            .setDuration(300)
            .withEndAction {
                binding.ivCorrect.animate()
                    .alpha(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .start()
            }
            .start()
    }

    private fun animateWrongAnswer() {
        binding.ivWrong.visibility = View.VISIBLE
        binding.ivWrong.alpha = 0f
        binding.ivWrong.animate()
            .alpha(1f)
            .scaleX(1.5f)
            .scaleY(1.5f)
            .setDuration(300)
            .withEndAction {
                binding.ivWrong.animate()
                    .alpha(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .start()
            }
            .start()
    }

    private fun loadNextQuestion() {
        currentQuestionIndex++

        if (currentQuestionIndex < questions.size) {
            // Animate transition
            binding.cardQuestion.animate()
                .translationX(-1000f)
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.cardQuestion.translationX = 1000f
                    displayQuestion()
                    binding.cardQuestion.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(300)
                        .start()
                }
                .start()
        } else {
            finishQuiz()
        }
    }

    private fun startTimer() {
        stopTimer() // Cancel any existing timer before starting a new one

        timer = object : CountDownTimer(Constants.TIME_PER_QUESTION * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                val seconds = (millisUntilFinished / 1000).toInt()
                binding.progressTimer.progress = seconds
                binding.tvTimer.text = getString(R.string.time_format, seconds)

                if (seconds <= 5) {
                    binding.tvTimer.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                }
            }

            override fun onFinish() {
                timeLeft = 0
                binding.tvTimer.text = getString(R.string.times_up)
                binding.tvTimer.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))

                // Auto-advance on timeout — does NOT call submitAnswer() to avoid toast loop
                onTimeUp()
            }
        }

        timer.start()
        isTimerRunning = true
    }

    private fun stopTimer() {
        if (isTimerRunning) {
            timer.cancel()
            isTimerRunning = false
        }
    }

    private fun updateProgress() {
        val progress = ((currentQuestionIndex + 1) * 100) / totalQuestions
        binding.progressBar.progress = progress
    }

    private fun finishQuiz() {
        stopTimer()

        // Update leaderboard
        lifecycleScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            firebaseHelper.updateLeaderboard(userId, correctAnswers)

            // Check for achievements
            val achievements = firebaseHelper.checkAndAwardAchievements(userId, categoryId, correctAnswers)

            // Navigate to result activity
            val intent = Intent(this@QuizActivity, ResultActivity::class.java).apply {
                putExtra("score", correctAnswers)
                putExtra("total", totalQuestions)
                putExtra("category", categoryName)
                putExtra("category_id", categoryId)
                val achievementNames = if (achievements.isSuccess) {
                    ArrayList(achievements.getOrDefault(emptyList()).map { it.name })
                } else {
                    ArrayList()
                }
                putStringArrayListExtra("achievements", achievementNames)
            }
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBarLoading.visibility = if (show) View.VISIBLE else View.GONE
        binding.cardQuestion.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        stopTimer()
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }
}