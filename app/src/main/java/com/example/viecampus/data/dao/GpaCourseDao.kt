package com.example.viecampus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.viecampus.data.entity.GpaCourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GpaCourseDao {
    @Query("SELECT * FROM gpa_courses ORDER BY id ASC")
    fun getCourses(): Flow<List<GpaCourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: GpaCourseEntity): Long

    @Update
    suspend fun update(course: GpaCourseEntity)

    @Query("DELETE FROM gpa_courses WHERE id = :id")
    suspend fun deleteById(id: Long)
}
