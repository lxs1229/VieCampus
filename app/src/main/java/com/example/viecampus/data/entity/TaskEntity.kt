package com.example.viecampus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.viecampus.model.TaskStatus
import com.example.viecampus.model.TaskType

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String?,
    val dueAt: Long?,
    val type: TaskType,
    val status: TaskStatus,
    val courseId: Long?
)
