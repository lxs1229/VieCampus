package com.example.viecampus.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.example.viecampus.data.CampusRepository
import com.example.viecampus.data.entity.CourseEntity
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val repository: CampusRepository
) : ViewModel() {

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

    fun deleteCourse(course: CourseEntity) {
        viewModelScope.launch {
            repository.deleteCourse(course)
        }
    }
}
