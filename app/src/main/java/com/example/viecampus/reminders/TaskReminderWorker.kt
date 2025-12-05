package com.example.viecampus.reminders

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import androidx.navigation.NavDeepLinkBuilder
import com.example.viecampus.R
import com.example.viecampus.data.CampusDatabase
import com.example.viecampus.data.CampusRepository
import com.example.viecampus.data.entity.TaskEntity
import com.example.viecampus.model.TaskStatus
import com.example.viecampus.model.TaskType
import com.example.viecampus.notifications.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val repository: CampusRepository by lazy {
        val database = CampusDatabase.getInstance(appContext)
        CampusRepository(database.courseDao(), database.taskDao(), database.gpaCourseDao())
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(KEY_TASK_ID, -1L)
        if (taskId <= 0) return Result.failure()

        val task = repository.getTaskById(taskId) ?: return Result.success()
        if (task.status == TaskStatus.DONE) return Result.success()

        val dueAt = task.dueAt ?: return Result.success()
        if (dueAt <= System.currentTimeMillis()) return Result.success()

        if (!hasNotificationPermission()) {
            return Result.success()
        }

        NotificationHelper.createReminderChannel(applicationContext)
        showNotification(task)

        return Result.success()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(task: TaskEntity) {
        val pendingIntent = NavDeepLinkBuilder(applicationContext)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.tasksFragment)
            .createPendingIntent()

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dueText = task.dueAt?.let { due ->
            formatter.format(Date(due))
        }

        val subTitleRes = when (task.type) {
            TaskType.ASSIGNMENT -> R.string.reminder_assignment_title
            TaskType.EXAM -> R.string.reminder_exam_title
            TaskType.TODO -> R.string.reminder_generic_title
        }

        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tasks)
            .setContentTitle(task.title)
            .setContentText(dueText?.let { applicationContext.getString(R.string.reminder_due_text, it) })
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        buildString {
                            append(applicationContext.getString(subTitleRes))
                            if (!task.description.isNullOrBlank()) {
                                append("\n")
                                append(task.description)
                            }
                            if (dueText != null) {
                                append("\n")
                                append(applicationContext.getString(R.string.reminder_due_text, dueText))
                            }
                        }
                    )
            )
            .setSubText(applicationContext.getString(subTitleRes))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(task.id.toInt(), notification)
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    companion object {
        const val KEY_TASK_ID = "task_id"

        fun createInput(taskId: Long) = workDataOf(KEY_TASK_ID to taskId)
    }
}
