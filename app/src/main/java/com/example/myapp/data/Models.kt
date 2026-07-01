package com.example.myapp.data

import com.google.gson.annotations.SerializedName

enum class Gender {
    @SerializedName("M") MALE,
    @SerializedName("F") FEMALE
}

enum class Priority(val level: Int) {
    LOW(1),
    MEDIUM(2),
    HIGH(3)
}

data class Person(
    val id: Long,
    val name: String,
    val gender: Gender,
    val active: Boolean = true,
    val unavailableDays: List<String> = emptyList()
)

data class Chore(
    val id: String,
    val label: String,
    val priority: Priority,
    val genderConstraint: Gender? = null
)

val CHORES = listOf(
    Chore("toilet_m", "🚽 שירותים בנים", Priority.HIGH, Gender.MALE),
    Chore("toilet_f", "🚺 שירותים בנות", Priority.HIGH, Gender.FEMALE),
    Chore("office", "🧹 ניקיון משרד", Priority.LOW),
    Chore("grass", "🌿 דשא", Priority.LOW),
    Chore("kitchen", "☕ מטבחון", Priority.MEDIUM),
    Chore("trash", "🗑️ פחים", Priority.MEDIUM)
)

val DAYS_HE = mapOf(
    "sun" to "ראשון",
    "mon" to "שני",
    "tue" to "שלישי",
    "wed" to "רביעי",
    "thu" to "חמישי"
)

val DAY_KEYS = listOf("sun", "mon", "tue", "wed", "thu")
