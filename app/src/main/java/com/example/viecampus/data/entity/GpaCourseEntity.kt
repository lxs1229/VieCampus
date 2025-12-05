package com.example.viecampus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gpa_courses")
data class GpaCourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val score: Double,
    val credits: Double
)
