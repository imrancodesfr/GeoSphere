package com.example.geosphere.utils

/**
 * Locally-defined achievement milestones based on cumulative correct answers.
 * No Firebase data needed for definitions — just check against User.correctAnswers.
 */
data class AchievementMilestone(
    val requiredCorrect: Int,
    val id: String,
    val name: String,
    val emoji: String,
    val description: String
)

object AchievementMilestones {

    val ALL = listOf(
        AchievementMilestone(
            requiredCorrect = 5,
            id = "just_started",
            name = "Just Started",
            emoji = "🌱",
            description = "Answered 5 questions correctly — the journey begins!"
        ),
        AchievementMilestone(
            requiredCorrect = 10,
            id = "transforming_beginner",
            name = "Transforming Beginner",
            emoji = "🌿",
            description = "10 correct answers — you're growing fast!"
        ),
        AchievementMilestone(
            requiredCorrect = 25,
            id = "getting_serious",
            name = "Getting Serious",
            emoji = "🗺️",
            description = "25 correct — you really know your geography!"
        ),
        AchievementMilestone(
            requiredCorrect = 50,
            id = "geo_geek",
            name = "Geo Geek",
            emoji = "🌍",
            description = "50 correct answers — you're officially a Geo Geek!"
        ),
        AchievementMilestone(
            requiredCorrect = 100,
            id = "here_to_stay",
            name = "You're Here to Stay",
            emoji = "🏆",
            description = "100 correct — true dedication to the world!"
        ),
        AchievementMilestone(
            requiredCorrect = 250,
            id = "world_explorer",
            name = "World Explorer",
            emoji = "🌐",
            description = "250 correct — you've mentally explored the whole planet!"
        ),
        AchievementMilestone(
            requiredCorrect = 500,
            id = "geography_master",
            name = "Geography Master",
            emoji = "🎓",
            description = "500 correct answers — a true master of geography!"
        ),
        AchievementMilestone(
            requiredCorrect = 1000,
            id = "globe_trotter_legend",
            name = "Globe Trotter Legend",
            emoji = "👑",
            description = "1000 correct — legendary status achieved!"
        )
    )

    /** Returns all milestones the user has earned given their total correct answers. */
    fun getEarned(totalCorrect: Int): List<AchievementMilestone> =
        ALL.filter { it.requiredCorrect <= totalCorrect }

    /** Returns all milestones the user has NOT yet earned. */
    fun getLocked(totalCorrect: Int): List<AchievementMilestone> =
        ALL.filter { it.requiredCorrect > totalCorrect }

    /** Returns the next milestone the user is working towards, or null if all earned. */
    fun getNext(totalCorrect: Int): AchievementMilestone? =
        ALL.firstOrNull { it.requiredCorrect > totalCorrect }
}
