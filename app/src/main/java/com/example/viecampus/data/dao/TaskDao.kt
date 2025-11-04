package com.example.viecampus.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.viecampus.data.entity.TaskEntity
import com.example.viecampus.model.TaskStatus
import com.example.viecampus.model.TaskType
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query(
        """
        SELECT * FROM tasks
        ORDER BY 
            CASE WHEN dueAt IS NULL THEN 1 ELSE 0 END,
            dueAt ASC
        """
    )
    fun getTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: TaskStatus)

    @Query(
        """
        SELECT * FROM tasks 
        WHERE dueAt IS NOT NULL AND dueAt > :now AND status != :completedStatus 
        AND (type = :assignmentType OR type = :examType)
        ORDER BY dueAt ASC
        """
    )
    suspend fun getUpcomingReminders(
        now: Long,
        completedStatus: TaskStatus,
        assignmentType: TaskType,
        examType: TaskType
    ): List<TaskEntity>
}
