package com.example.viecampus.reminders

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class ReminderScheduler(context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun scheduleReminder(taskId: Long, triggerAtMillis: Long) {
        val delay = triggerAtMillis - System.currentTimeMillis()
        if (delay <= 0) return

        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(TaskReminderWorker.createInput(taskId))
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName(taskId),
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelReminder(taskId: Long) {
        workManager.cancelUniqueWork(uniqueWorkName(taskId))
    }

    private fun uniqueWorkName(taskId: Long): String = "task_reminder_$taskId"
}
