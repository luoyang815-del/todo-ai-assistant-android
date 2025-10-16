
package com.example.todoai.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ToDo::class, Summary::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun todoDao(): ToDoDao
    abstract fun summaryDao(): SummaryDao
}
