package com.example.mindspace.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CheckIn") // ✅ MUST match DAO query
data class CheckIn(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mood: Int,
    val notes: String,
    val tags: String,
    val timestamp: Long = System.currentTimeMillis()
)

