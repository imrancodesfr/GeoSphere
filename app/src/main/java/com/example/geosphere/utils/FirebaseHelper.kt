package com.example.geosphere.utils

import com.example.geosphere.models.*
import com.example.geosphere.theme.ColorPalette
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseHelper {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    // References
    private val usersRef = database.getReference("users")
    private val questionsRef = database.getReference("questions")
    private val categoriesRef = database.getReference("categories")
    private val leaderboardRef = database.getReference("leaderboard")
    private val achievementsRef = database.getReference("achievements")
    private val userAnswersRef = database.getReference("user_answers")

    // ==========================
    // AUTHENTICATION
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

            usersRef.child(userId).setValue(user).await()
            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Login failed")

            val snapshot = usersRef.child(userId).get().await()
            val user = snapshot.getValue(User::class.java)
                ?: throw Exception("User not found")

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================
    // QUESTIONS (🔥 FIXED)
    // ==========================

    suspend fun getQuestionsByCategory(category: String): Result<List<Question>> {
        return try {
            val snapshot = questionsRef.child(category).get().await()

            if (!snapshot.exists()) {
                return Result.success(emptyList())
            }

            val questions = mutableListOf<Question>()

            for (child in snapshot.children) {

                // Read raw map safely
                val map = child.value as? Map<*, *> ?: continue

                val questionText = map["questionText"] as? String ?: ""
                val correctIndex = (map["correctOptionIndex"] as? Long)?.toInt() ?: 0
                val explanation = map["explanation"] as? String ?: ""
                val difficulty = map["difficulty"] as? String ?: "medium"
                val points = (map["points"] as? Long)?.toInt() ?: 1

                // 🔥 FIX: Handle options as Map → List
                val optionsMap = map["options"] as? Map<*, *> ?: emptyMap<Any, Any>()

                val optionsList = optionsMap
                    .toSortedMap(compareBy { it.toString() }) // ensures 0,1,2,3 order
                    .values
                    .map { it.toString() }

                val question = Question(
                    id = child.key ?: "",
                    questionText = questionText,
                    options = optionsList,
                    correctOptionIndex = correctIndex,
                    explanation = explanation,
                    difficulty = difficulty,
                    points = points
                )

                questions.add(question)
            }

            questions.shuffle()

            Result.success(questions)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================
    // USER
    // ==========================

    suspend fun createUser(user: User): Result<Boolean> {
        return try {
            usersRef.child(user.uid).setValue(user).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addQuestion(category: String, question: Question): Result<Boolean> {
        return try {
            val ref = questionsRef.child(category).push()
            val questionWithId = question.copy(id = ref.key ?: "")
            ref.setValue(questionWithId).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================
    // LEADERBOARD
    // ==========================

    suspend fun updateLeaderboard(userId: String, points: Int): Result<Boolean> {
        return try {
            val snapshot = usersRef.child(userId).get().await()
            val user = snapshot.getValue(User::class.java)
                ?: return Result.failure(Exception("User not found"))

            val updatedUser = user.copy(
                totalPoints = user.totalPoints + points,
                quizzesPlayed = user.quizzesPlayed + 1,
                correctAnswers = user.correctAnswers + points
            )

            usersRef.child(userId).setValue(updatedUser).await()

            val entry = LeaderboardEntry(
                userId = userId,
                username = user.username,
                totalPoints = updatedUser.totalPoints,
                quizzesPlayed = updatedUser.quizzesPlayed,
                correctAnswers = updatedUser.correctAnswers,
                lastUpdated = System.currentTimeMillis()
            )

            leaderboardRef.child(userId).setValue(entry).await()

            Result.success(true)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLeaderboard(): Result<List<LeaderboardEntry>> {
        return try {
            val snapshot = leaderboardRef
                .orderByChild("totalPoints")
                .limitToLast(100)
                .get()
                .await()

            val list = mutableListOf<LeaderboardEntry>()

            for (child in snapshot.children) {
                val entry = child.getValue(LeaderboardEntry::class.java)
                entry?.let { list.add(it) }
            }

            list.sortByDescending { it.totalPoints }

            val ranked = list.mapIndexed { index, item ->
                item.copy(rank = index + 1)
            }

            Result.success(ranked)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================
    // USER ANSWERS
    // ==========================

    suspend fun hasUserAnsweredQuestion(userId: String, questionId: String): Boolean {
        return try {
            userAnswersRef.child(userId).child(questionId).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun recordUserAnswer(
        userId: String,
        questionId: String,
        isCorrect: Boolean
    ): Result<Boolean> {
        return try {
            val data = mapOf(
                "questionId" to questionId,
                "isCorrect" to isCorrect,
                "timestamp" to System.currentTimeMillis()
            )

            userAnswersRef.child(userId).child(questionId).setValue(data).await()
            Result.success(true)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================
    // ACHIEVEMENTS
    // ==========================

    suspend fun checkAndAwardAchievements(
        userId: String,
        category: String,
        score: Int
    ): Result<List<Achievement>> {

        return try {
            val userSnapshot = usersRef.child(userId).get().await()
            val user = userSnapshot.getValue(User::class.java)
                ?: return Result.failure(Exception("User not found"))

            val achievementSnapshot = achievementsRef.child(category).get().await()

            val newAchievements = mutableListOf<Achievement>()
            val updated = user.achievements.toMutableList()

            for (child in achievementSnapshot.children) {
                val achievement = child.getValue(Achievement::class.java)

                achievement?.let {
                    if (score >= it.requiredScore && !updated.contains(it.id)) {
                        updated.add(it.id)
                        newAchievements.add(it)
                    }
                }
            }

            if (newAchievements.isNotEmpty()) {
                usersRef.child(userId).child("achievements").setValue(updated).await()
            }

            Result.success(newAchievements)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================
    // ADMIN
    // ==========================

    suspend fun verifyAdmin(userId: String): Boolean {
        return try {
            val snapshot = usersRef.child(userId).get().await()
            val user = snapshot.getValue(User::class.java)
            user?.isAdmin == true
        } catch (e: Exception) {
            false
        }
    }

    // ==========================
    // INITIAL DATA
    // ==========================

    suspend fun initializeDatabase() {
        try {
            val snapshot = categoriesRef.get().await()

            if (!snapshot.exists()) {
                val categories = listOf(
                    Category("asia", "Asia", "🌏", "Largest continent", 0, ColorPalette.BoldBlue1),
                    Category("europe", "Europe", "🌍", "European countries", 0, ColorPalette.Turquoise),
                    Category("americas", "Americas", "🌎", "North & South America", 0, ColorPalette.BoldOrange),
                    Category("africa", "Africa", "🌍", "Cradle of humanity", 0, ColorPalette.SubtleGreen),
                    Category("flags", "Flags", "🏁", "Guess flags", 0, ColorPalette.StrikingRed),
                    Category("capitals", "Capitals", "🏛️", "World capitals", 0, ColorPalette.AccentPurple),
                    Category("landmarks", "Landmarks", "🗽", "Famous places", 0, ColorPalette.DeepTeal),
                    Category("world", "World", "🌐", "Mixed quiz", 0, ColorPalette.BoldBlue3)
                )

                categories.forEach {
                    categoriesRef.child(it.id).setValue(it).await()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
