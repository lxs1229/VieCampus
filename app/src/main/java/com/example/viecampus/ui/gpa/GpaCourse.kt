package com.example.viecampus.ui.gpa

data class GpaCourse(
    val id: Long,
    val name: String,
    val score: Double,
    val credits: Double
) {
    val gradePoint: Double
        get() = when {
            score >= 90 -> 4.0
            score >= 85 -> 3.7
            score >= 82 -> 3.3
            score >= 78 -> 3.0
            score >= 75 -> 2.7
            score >= 72 -> 2.3
            score >= 68 -> 2.0
            score >= 64 -> 1.5
            score >= 60 -> 1.0
            else -> 0.0
        }
}
