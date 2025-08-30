package com.example.mindspace.data

import android.content.Context
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CheckInRepository private constructor(context: Context) {

    private val checkInDao = CheckInDatabase.getDatabase(context).checkInDao()
    private val firestore = FirebaseFirestore.getInstance()

    // ✅ LiveData for observing in real-time
    val allLocalCheckIns: LiveData<List<CheckIn>> = checkInDao.getAllCheckIns()

    // ✅ One-time fetch of check-ins (not observable)
    suspend fun getAllCheckInsOnce(): List<CheckIn> {
        return withContext(Dispatchers.IO) {
            checkInDao.getAllCheckInsList() // ✅ Corrected method name
        }
    }

    // ✅ Save to Room and upload to Firestore
    suspend fun saveCheckIn(checkIn: CheckIn) {
        checkInDao.insert(checkIn)
        uploadToFirestore(checkIn)
    }

    // ✅ Delete from Room
    suspend fun deleteLocal(checkIn: CheckIn) {
        withContext(Dispatchers.IO) {
            checkInDao.delete(checkIn)
        }
    }

    // ✅ Firestore upload logic
    private fun uploadToFirestore(checkIn: CheckIn) {
        val data = hashMapOf(
            "mood" to checkIn.mood,
            "notes" to checkIn.notes,
            "tags" to checkIn.tags,
            "timestamp" to checkIn.timestamp
        )

        firestore.collection("checkins")
            .add(data)
            .addOnSuccessListener { doc ->
                println("✅ Uploaded to Firestore: ${doc.id}")
            }
            .addOnFailureListener { e ->
                println("❌ Firestore upload failed: $e")
            }
    }

    // ✅ Singleton pattern
    companion object {
        @Volatile
        private var INSTANCE: CheckInRepository? = null

        fun getInstance(context: Context): CheckInRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CheckInRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
