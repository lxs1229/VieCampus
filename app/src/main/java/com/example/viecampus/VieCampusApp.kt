package com.example.viecampus

import android.app.Application
import com.example.viecampus.data.CampusDatabase
import com.example.viecampus.data.CampusRepository
import com.example.viecampus.notifications.NotificationHelper

class VieCampusApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createReminderChannel(this)
    }

    val database: CampusDatabase by lazy {
        CampusDatabase.getInstance(this)
    }

    val repository: CampusRepository by lazy {
        CampusRepository(database.courseDao(), database.taskDao(), database.gpaCourseDao())
    }
}
