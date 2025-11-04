package com.example.viecampus.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.viecampus.data.CampusRepository
import com.example.viecampus.reminders.ReminderScheduler

class TasksViewModelFactory(
    private val repository: CampusRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TasksViewModel(repository, reminderScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
