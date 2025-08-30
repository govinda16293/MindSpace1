package com.example.mindspace.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.mindspace.data.CheckInDatabase
import com.example.mindspace.data.JournalDao
import com.example.mindspace.data.JournalEntry
import com.example.mindspace.network.EmotionRequest
import com.example.mindspace.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val journalDao: JournalDao =
        CheckInDatabase.getDatabase(application).journalDao()

    val allEntries: LiveData<List<JournalEntry>> = journalDao.getAllEntries()

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            journalDao.delete(entry)
        }
    }

    private fun containsRedFlags(content: String): Boolean {
        val redFlags = listOf(
            "suicidal", "i want to die", "kill myself", "hopeless", "worthless",
            "end it all", "i can’t go on", "give up", "i hate my life", "i feel empty",
            "self harm", "cut myself", "i’m done", "life is meaningless",
            "no reason to live", "i want to disappear", "i wish i wasn't born",
            "overwhelmed", "broken", "i feel alone", "tired of everything"
        )
        return redFlags.any { keyword ->
            content.lowercase().contains(keyword)
        }
    }

    fun addEntryWithCallback(
        title: String?,
        content: String,
        onRedFlagDetected: () -> Unit,
        onSafeToSave: (emotion: String?) -> Unit
    ) {
        if (containsRedFlags(content)) {
            onRedFlagDetected()
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                val entry = JournalEntry(title = title, content = content)
                journalDao.insert(entry)

                detectEmotion(content) { emotion ->
                    onSafeToSave(emotion)
                }
            }
        }
    }

    private fun detectEmotion(text: String, onResult: (String?) -> Unit) {
        val request = EmotionRequest(inputs = text)
        RetrofitClient.emotionApiService.analyzeMood(request)
            .enqueue(object : Callback<List<Map<String, Any>>> {
                override fun onResponse(
                    call: Call<List<Map<String, Any>>>,
                    response: Response<List<Map<String, Any>>>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        val top = body?.maxByOrNull { it["score"] as Double }
                        val emotion = top?.get("label") as? String
                        onResult(emotion)
                    } else {
                        Log.e("EmotionAPI", "Failed: ${response.errorBody()?.string()}")
                        onResult(null)
                    }
                }

                override fun onFailure(call: Call<List<Map<String, Any>>>, t: Throwable) {
                    Log.e("EmotionAPI", "Error: ${t.message}")
                    onResult(null)
                }
            })
    }
}
