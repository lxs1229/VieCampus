package com.example.viecampus.ui.gpa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class GpaViewModel : ViewModel() {

    private val _courses = MutableStateFlow<List<GpaCourse>>(emptyList())
    val courses: StateFlow<List<GpaCourse>> = _courses.asStateFlow()

    val gpa: StateFlow<Double> = _courses
        .map { computeGpa(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    private var nextId = 1L

    fun saveCourse(course: GpaCourse) {
        _courses.update { current ->
            if (course.id == 0L) {
                current + course.copy(id = nextId++)
            } else {
                current.map { existing ->
                    if (existing.id == course.id) course else existing
                }
            }
        }
    }

    fun deleteCourse(id: Long) {
        _courses.update { current ->
            current.filterNot { it.id == id }
        }
    }

    private fun computeGpa(courses: List<GpaCourse>): Double {
        val totalCredits = courses.sumOf { it.credits }
        if (totalCredits == 0.0) return 0.0
        val totalPoints = courses.sumOf { it.gradePoint * it.credits }
        return totalPoints / totalCredits
    }
}
