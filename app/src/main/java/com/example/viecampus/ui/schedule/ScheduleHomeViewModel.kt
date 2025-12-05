package com.example.viecampus.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.viecampus.data.CampusRepository
import com.example.viecampus.data.entity.CourseEntity
import kotlinx.coroutines.launch

class ScheduleHomeViewModel(
    private val repository: CampusRepository
) : ViewModel() {
    val tasks = repository.tasks.asLiveData()
    val courses = repository.courses.asLiveData()

    fun saveCourse(course: CourseEntity) {
        viewModelScope.launch {
            if (course.id == 0L) {
                repository.addCourse(course)
            } else {
                repository.updateCourse(course)
            }
        }
    }
}

class ScheduleHomeViewModelFactory(
    private val repository: CampusRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleHomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleHomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
