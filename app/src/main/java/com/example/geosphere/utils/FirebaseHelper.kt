package com.example.geosphere.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.geosphere.models.*
import com.example.geosphere.theme.ColorPalette
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class FirebaseHelper(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val prefs: SharedPreferences = context.getSharedPreferences("GeoSphereLocalDb", Context.MODE_PRIVATE)
    private val gson = Gson()

    // ==========================
    // AUTHENTICATION (Firebase Auth + Local Profile)
    // ==========================

    suspend fun registerUser(email: String, password: String, username: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User creation failed")

            val user = User(
                uid = userId,
                email = email,
                username = username,
                createdAt = System.currentTimeMillis()
            )

            // Save user profile locally instead of Firebase DB
            prefs.edit().putString("user_profile_$userId", gson.toJson(user)).apply()
            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Login failed")

            val userJson = prefs.getString("user_profile_$userId", null)
                ?: throw Exception("User profile not found locally")

            val user = gson.fromJson(userJson, User::class.java)
            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper for internal use to get local user profile
    private fun getLocalUser(userId: String): User? {
        val json = prefs.getString("user_profile_$userId", null) ?: return null
        return gson.fromJson(json, User::class.java)
    }

    private fun saveLocalUser(user: User) {
        prefs.edit().putString("user_profile_${user.uid}", gson.toJson(user)).apply()
    }

    // ==========================
    // QUESTIONS (From local assets JSON)
    // ==========================

    suspend fun getQuestionsByCategory(category: String): Result<List<Question>> {
        return withContext(Dispatchers.IO) {
            try {
                // Read from local assets JSON instead of Firebase
                val inputStream: InputStream = context.assets.open("questions.json")
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                
                val rootObject = JSONObject(jsonString)
                if (!rootObject.has("questions")) return@withContext Result.success(emptyList())
                
                val categoriesObj = rootObject.getJSONObject("questions")
                if (!categoriesObj.has(category)) return@withContext Result.success(emptyList())

                val categoryObj = categoriesObj.getJSONObject(category)
                val questions = mutableListOf<Question>()

                val keys = categoryObj.keys()
                while (keys.hasNext()) {
                    val qId = keys.next()
                    val qObj = categoryObj.getJSONObject(qId)

                    val questionText = qObj.optString("questionText", "")
                    val correctIndex = qObj.optInt("correctOptionIndex", 0)
                    val explanation = qObj.optString("explanation", "")
                    val difficulty = qObj.optString("difficulty", "medium")
                    val points = qObj.optInt("points", 1)

                    val optionsList = mutableListOf<String>()
                    if (qObj.has("options")) {
                        val optObj = qObj.getJSONObject("options")
                        // Ensure we extract options 0 to 3 in order
                        for (i in 0..3) {
                            if (optObj.has(i.toString())) {
                                optionsList.add(optObj.getString(i.toString()))
                            }
                        }
                    }

                    questions.add(
                        Question(
                            id = qId,
                            questionText = questionText,
                            options = optionsList,
                            correctOptionIndex = correctIndex,
                            explanation = explanation,
                            difficulty = difficulty,
                            points = points
                        )
                    )
                }

                questions.shuffle()
                Result.success(questions)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ==========================
    // LEADERBOARD (Local SharedPreferences)
    // ==========================

    private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private fun thisWeekKey(): String {
        val cal = Calendar.getInstance()
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        val year = cal.get(Calendar.YEAR)
        return "$year-W${week.toString().padStart(2, '0')}"
    }

    suspend fun updateLeaderboard(userId: String, points: Int): Result<Boolean> {
        return try {
            val user = getLocalUser(userId) ?: return Result.failure(Exception("Local User missing"))

            val updatedUser = user.copy(
                totalPoints = user.totalPoints + points,
                quizzesPlayed = user.quizzesPlayed + 1,
                correctAnswers = user.correctAnswers + points
            )
            saveLocalUser(updatedUser)

            // Read all leaderboard entries
            val type = object : TypeToken<MutableList<LeaderboardEntry>>() {}.type
            val leaderboardJson = prefs.getString("local_leaderboard", "[]")
            val allEntries: MutableList<LeaderboardEntry> = gson.fromJson(leaderboardJson, type)

            // Find existing
            val existingIndex = allEntries.indexOfFirst { it.userId == userId }
            val existing = if (existingIndex >= 0) allEntries[existingIndex] else null

            val today = todayKey()
            val thisWeek = thisWeekKey()

            val dailyPoints = if (existing?.dailyKey == today) existing.dailyPoints + points else points
            val weeklyPoints = if (existing?.weeklyKey == thisWeek) existing.weeklyPoints + points else points

            val entry = LeaderboardEntry(
                userId = userId,
                username = user.username,
                totalPoints = updatedUser.totalPoints,
                quizzesPlayed = updatedUser.quizzesPlayed,
                correctAnswers = updatedUser.correctAnswers,
                lastUpdated = System.currentTimeMillis(),
                dailyPoints = dailyPoints,
                dailyKey = today,
                weeklyPoints = weeklyPoints,
                weeklyKey = thisWeek
            )

            if (existingIndex >= 0) {
                allEntries[existingIndex] = entry
            } else {
                allEntries.add(entry)
            }

            prefs.edit().putString("local_leaderboard", gson.toJson(allEntries)).apply()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLeaderboardByType(filterType: String = "all"): Result<List<LeaderboardEntry>> {
        return try {
            val type = object : TypeToken<List<LeaderboardEntry>>() {}.type
            val leaderboardJson = prefs.getString("local_leaderboard", "[]")
            val list: List<LeaderboardEntry> = gson.fromJson(leaderboardJson, type)

            val today = todayKey()
            val thisWeek = thisWeekKey()

            val sorted = when (filterType) {
                "daily" -> list.filter { it.dailyKey == today && it.dailyPoints > 0 }.sortedByDescending { it.dailyPoints }
                "weekly" -> list.filter { it.weeklyKey == thisWeek && it.weeklyPoints > 0 }.sortedByDescending { it.weeklyPoints }
                else -> list.sortedByDescending { it.totalPoints }
            }

            val ranked = sorted.mapIndexed { index, item -> item.copy(rank = index + 1) }
            Result.success(ranked)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLeaderboard(): Result<List<LeaderboardEntry>> = getLeaderboardByType("all")

    // ==========================
    // USER ANSWERS
    // ==========================

    suspend fun hasUserAnsweredQuestion(userId: String, questionId: String): Boolean {
        return prefs.getBoolean("answered_${userId}_${questionId}", false)
    }

    suspend fun recordUserAnswer(userId: String, questionId: String, isCorrect: Boolean): Result<Boolean> {
        prefs.edit().putBoolean("answered_${userId}_${questionId}", true).apply()
        return Result.success(true)
    }

    // ==========================
    // ACHIEVEMENTS
    // ==========================

    suspend fun checkAndAwardAchievements(userId: String, category: String, score: Int): Result<List<Achievement>> {
        return try {
            val user = getLocalUser(userId) ?: return Result.failure(Exception("User not found locally"))
            val totalCorrect = user.correctAnswers
            val earnedIds = user.achievements.toMutableList()

            val newlyUnlocked = AchievementMilestones.ALL.filter { milestone ->
                milestone.requiredCorrect <= totalCorrect && !earnedIds.contains(milestone.id)
            }

            if (newlyUnlocked.isNotEmpty()) {
                newlyUnlocked.forEach { earnedIds.add(it.id) }
                saveLocalUser(user.copy(achievements = earnedIds))
            }

            val result = newlyUnlocked.map { m ->
                Achievement(id = m.id, name = "${m.emoji} ${m.name}", description = m.description, requiredScore = m.requiredCorrect)
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserAchievements(userId: String): Result<List<AchievementMilestone>> {
        val user = getLocalUser(userId) ?: return Result.failure(Exception("User not found locally"))
        return Result.success(AchievementMilestones.getEarned(user.correctAnswers))
    }

    suspend fun getAllMilestonesWithStatus(userId: String): Result<Pair<List<AchievementMilestone>, Int>> {
        val user = getLocalUser(userId) ?: return Result.failure(Exception("User not found locally"))
        return Result.success(Pair(AchievementMilestones.ALL, user.correctAnswers))
    }

    // ==========================
    // ADMIN (Stubs)
    // ==========================
    suspend fun verifyAdmin(userId: String): Boolean = false
    suspend fun initializeDatabase() {}
    suspend fun createUser(user: User): Result<Boolean> {
        saveLocalUser(user)
        return Result.success(true)
    }
    suspend fun addQuestion(category: String, question: Question): Result<Boolean> = Result.success(true)
}
