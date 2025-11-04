package com.example.viecampus.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.viecampus.data.dao.CourseDao
import com.example.viecampus.data.dao.TaskDao
import com.example.viecampus.data.entity.CourseEntity
import com.example.viecampus.data.entity.TaskEntity

@Database(
    entities = [CourseEntity::class, TaskEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(CampusConverters::class)
abstract class CampusDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: CampusDatabase? = null

        fun getInstance(context: Context): CampusDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CampusDatabase::class.java,
                    "vie_campus.db"
                ).build().also { INSTANCE = it }
            }
    }
}
