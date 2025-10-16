
package com.example.todoai.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoDao {
    @Query("SELECT * FROM todos ORDER BY dueAt IS NULL, dueAt ASC")
    fun watchAll(): Flow<List<ToDo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ToDo)

    @Query("UPDATE todos SET status=:status WHERE id=:id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM todos WHERE id=:id")
    suspend fun delete(id: String)
}

@Dao
interface SummaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: Summary)

    @Query("SELECT * FROM summaries ORDER BY createdAt DESC LIMIT 20")
    fun latest(): Flow<List<Summary>>
}
