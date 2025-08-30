package com.example.mindspace.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mindspace.data.JournalEntry

@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntry)

    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntries(): LiveData<List<JournalEntry>>

    @Delete
    suspend fun delete(entry: JournalEntry)
}

