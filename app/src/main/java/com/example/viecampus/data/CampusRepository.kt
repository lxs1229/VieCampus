package com.example.viecampus.data

import com.example.viecampus.data.dao.CourseDao
import com.example.viecampus.data.dao.TaskDao
import com.example.viecampus.data.entity.CourseEntity
import com.example.viecampus.data.entity.TaskEntity
import com.example.viecampus.model.TaskStatus
import com.example.viecampus.model.TaskType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CampusRepository(
    private val courseDao: CourseDao,
    private val taskDao: TaskDao
) {

    val courses: Flow<List<CourseEntity>> = courseDao.getCourses()
    val tasks: Flow<List<TaskEntity>> = taskDao.getTasks()

    suspend fun addCourse(course: CourseEntity): Long = withContext(Dispatchers.IO) {
        courseDao.insert(course)
    }

    suspend fun updateCourse(course: CourseEntity) = withContext(Dispatchers.IO) {
        courseDao.update(course)
    }

    suspend fun deleteCourse(course: CourseEntity) = withContext(Dispatchers.IO) {
        courseDao.delete(course)
    }

    suspend fun addTask(task: TaskEntity): Long = withContext(Dispatchers.IO) {
        taskDao.insert(task)
    }

    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.update(task)
    }

    suspend fun deleteTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.delete(task)
    }

    suspend fun updateTaskStatus(id: Long, status: TaskStatus) = withContext(Dispatchers.IO) {
        taskDao.updateStatus(id, status)
    }

    suspend fun getTaskById(id: Long): TaskEntity? = withContext(Dispatchers.IO) {
        taskDao.getById(id)
    }

    suspend fun getUpcomingReminders(now: Long): List<TaskEntity> = withContext(Dispatchers.IO) {
        taskDao.getUpcomingReminders(now, TaskStatus.DONE, TaskType.ASSIGNMENT, TaskType.EXAM)
    }
}
