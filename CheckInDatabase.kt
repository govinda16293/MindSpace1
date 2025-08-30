package com.example.mindspace.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * ✅ App's Room Database for:
 * - Daily Emotional Check-Ins
 * - Journal Entries
 */
@Database(
    entities = [CheckIn::class, JournalEntry::class],
    version = 2,
    exportSchema = false
)
abstract class CheckInDatabase : RoomDatabase() {

    abstract fun checkInDao(): CheckInDao
    abstract fun journalDao(): JournalDao

    companion object {
        @Volatile
        private var INSTANCE: CheckInDatabase? = null

        fun getDatabase(context: Context): CheckInDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CheckInDatabase::class.java,
                    "checkin_database"
                )
                    // ✅ Automatically wipes & recreates DB if schema changes
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
