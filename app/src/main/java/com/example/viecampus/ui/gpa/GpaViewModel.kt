package com.example.viecampus.ui.gpa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.viecampus.data.CampusRepository
import com.example.viecampus.data.entity.GpaCourseEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GpaViewModel(
    private val repository: CampusRepository
) : ViewModel() {

    private val coursesState = repository.gpaCourses
        .map { entities -> entities.map(GpaCourseEntity::toModel) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val courses: StateFlow<List<GpaCourse>> = coursesState

    val gpa: StateFlow<Double> = coursesState
        .map { computeGpa(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun saveCourse(course: GpaCourse) {
        viewModelScope.launch {
            if (course.id == 0L) {
                repository.addGpaCourse(course.toEntity())
            } else {
                repository.updateGpaCourse(course.toEntity())
            }
        }
    }

    fun deleteCourse(id: Long) {
        viewModelScope.launch {
            repository.deleteGpaCourse(id)
        }
    }

    private fun computeGpa(courses: List<GpaCourse>): Double {
        val totalCredits = courses.sumOf { it.credits }
        if (totalCredits == 0.0) return 0.0
        val totalPoints = courses.sumOf { it.score * it.credits }
        return totalPoints / totalCredits
    }
}

private fun GpaCourseEntity.toModel(): GpaCourse =
    GpaCourse(
        id = id,
        name = name,
        score = score,
        credits = credits
    )

private fun GpaCourse.toEntity(): GpaCourseEntity =
    GpaCourseEntity(
        id = id,
        name = name,
        score = score,
        credits = credits
    )

class GpaViewModelFactory(
    private val repository: CampusRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GpaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GpaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
