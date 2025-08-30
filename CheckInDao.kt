package com.example.mindspace.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CheckInDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkIn: CheckIn)

    @Delete
    suspend fun delete(checkIn: CheckIn)

    // ✅ For observing check-ins in real time (for HistoryFragment or list views)
    @Query("SELECT * FROM CheckIn ORDER BY timestamp DESC")
    fun getAllCheckIns(): LiveData<List<CheckIn>>

    // ✅ For snapshot use in Dashboard (ordered ASC for graph)
    @Query("SELECT * FROM CheckIn ORDER BY timestamp ASC")
    suspend fun getAllCheckInsList(): List<CheckIn>
}

