package com.example.viecampus.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.viecampus.data.dao.CourseDao
import com.example.viecampus.data.dao.GpaCourseDao
import com.example.viecampus.data.dao.TaskDao
import com.example.viecampus.data.entity.CourseEntity
import com.example.viecampus.data.entity.GpaCourseEntity
import com.example.viecampus.data.entity.TaskEntity

@Database(
    entities = [CourseEntity::class, TaskEntity::class, GpaCourseEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(CampusConverters::class)
abstract class CampusDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao

    abstract fun taskDao(): TaskDao

    abstract fun gpaCourseDao(): GpaCourseDao

    companion object {
        @Volatile
        private var INSTANCE: CampusDatabase? = null

        fun getInstance(context: Context): CampusDatabase =
            INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                CampusDatabase::class.java,
                "vie_campus.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { INSTANCE = it }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS `gpa_courses` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `name` TEXT NOT NULL,
                            `score` REAL NOT NULL,
                            `credits` REAL NOT NULL
                        )
                    """.trimIndent()
                )
            }
        }
    }
}
