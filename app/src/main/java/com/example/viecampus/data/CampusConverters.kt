package com.example.viecampus.data

import androidx.room.TypeConverter
import com.example.viecampus.model.TaskStatus
import com.example.viecampus.model.TaskType

class CampusConverters {
    @TypeConverter
    fun fromTaskType(value: TaskType?): String? = value?.name

    @TypeConverter
    fun toTaskType(value: String?): TaskType? = value?.let(TaskType::valueOf)

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus?): String? = value?.name

    @TypeConverter
    fun toTaskStatus(value: String?): TaskStatus? = value?.let(TaskStatus::valueOf)
}
