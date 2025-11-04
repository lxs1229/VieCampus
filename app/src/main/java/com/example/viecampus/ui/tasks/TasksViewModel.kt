package com.example.viecampus.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.viecampus.data.CampusRepository
import com.example.viecampus.data.entity.TaskEntity
import com.example.viecampus.model.TaskStatus
import com.example.viecampus.model.TaskType
import com.example.viecampus.reminders.ReminderScheduler
import kotlinx.coroutines.launch

class TasksViewModel(
    private val repository: CampusRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    val tasks = repository.tasks.asLiveData()

    fun saveTask(task: TaskEntity) {
        viewModelScope.launch {
            val taskWithId = if (task.id == 0L) {
                val id = repository.addTask(task)
                task.copy(id = id)
            } else {
                repository.updateTask(task)
                task
            }
            handleReminder(taskWithId)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            reminderScheduler.cancelReminder(task.id)
        }
    }

    fun updateStatus(id: Long, status: TaskStatus) {
        viewModelScope.launch {
            repository.updateTaskStatus(id, status)
            repository.getTaskById(id)?.let { updated ->
                handleReminder(updated)
            }
        }
    }

    private fun handleReminder(task: TaskEntity) {
        if (shouldSchedule(task)) {
            val dueAt = task.dueAt ?: return
            reminderScheduler.scheduleReminder(task.id, dueAt)
        } else {
            reminderScheduler.cancelReminder(task.id)
        }
    }

    private fun shouldSchedule(task: TaskEntity): Boolean {
        val dueAt = task.dueAt ?: return false
        if (dueAt <= System.currentTimeMillis()) return false
        if (task.status == TaskStatus.DONE) return false
        return task.type == TaskType.ASSIGNMENT || task.type == TaskType.EXAM
    }
}
