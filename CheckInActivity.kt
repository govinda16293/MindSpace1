package com.example.mindspace.ui.checkin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindspace.R
import com.example.mindspace.adapter.CheckInAdapter
import com.example.mindspace.data.CheckIn
import com.example.mindspace.data.CheckInDatabase
import com.example.mindspace.data.CheckInRepository
import com.example.mindspace.network.EmotionRequest
import com.example.mindspace.network.RetrofitClient
import com.example.mindspace.viewmodel.CheckInViewModel
import com.example.mindspace.viewmodel.CheckInViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckInActivity : AppCompatActivity() {

    private lateinit var moodSeekBar: SeekBar
    private lateinit var moodEmoji: TextView
    private lateinit var moodValue: TextView
    private lateinit var tagChipGroup: ChipGroup
    private lateinit var notesEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var checkInRecyclerView: RecyclerView
    private lateinit var checkInAdapter: CheckInAdapter
    private lateinit var viewModel: CheckInViewModel

    private val emojis = listOf("😭", "😟", "😐", "🙂", "😄")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_check_in)

        // 1. Bind UI components
        moodSeekBar = findViewById(R.id.moodSeekBar)
        moodEmoji = findViewById(R.id.moodEmoji)
        moodValue = findViewById(R.id.moodValue)
        tagChipGroup = findViewById(R.id.tagChipGroup)
        notesEditText = findViewById(R.id.extraNotes)
        saveButton = findViewById(R.id.saveCheckIn)
        checkInRecyclerView = findViewById(R.id.checkInRecyclerView)

        // 2. Setup ViewModel
        val dao = CheckInDatabase.getDatabase(application).checkInDao()
        val repository = CheckInRepository.getInstance(application)
        val factory = CheckInViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[CheckInViewModel::class.java]

        // 3. Setup RecyclerView
        checkInAdapter = CheckInAdapter()
        checkInRecyclerView.layoutManager = LinearLayoutManager(this)
        checkInRecyclerView.adapter = checkInAdapter

        viewModel.allCheckIns.observe(this) { checkIns ->
            checkInAdapter.submitList(checkIns)
        }

        // 4. SeekBar listener
        moodSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                moodEmoji.text = emojis.getOrElse(progress) { "😐" }
                moodValue.text = "Mood: ${progress + 1}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 5. Save Button click
        saveButton.setOnClickListener {
            val mood = moodSeekBar.progress + 1
            val notes = notesEditText.text.toString()
            val selectedTags = getSelectedTags()

            val checkIn = CheckIn(
                mood = mood,
                notes = notes,
                tags = selectedTags.joinToString(",")
            )

            // 6. Save to Room + Firestore
            viewModel.saveCheckIn(checkIn)
            Toast.makeText(this, "✅ Check-in saved!", Toast.LENGTH_SHORT).show()

            // 7. Emotion Detection (HuggingFace)
            if (notes.isNotBlank()) {
                val emotionRequest = EmotionRequest(inputs = notes)

                RetrofitClient.emotionApiService.analyzeMood(emotionRequest)
                    .enqueue(object : Callback<List<Map<String, Any>>> {
                        override fun onResponse(
                            call: Call<List<Map<String, Any>>>,
                            response: Response<List<Map<String, Any>>>
                        ) {
                            if (response.isSuccessful) {
                                val resultList = response.body()
                                val topResult = resultList?.firstOrNull()
                                val emotion = topResult?.get("label") as? String ?: "Unknown"
                                val confidence = (topResult?.get("score") as? Double)?.toFloat() ?: 0f

                                Toast.makeText(
                                    this@CheckInActivity,
                                    "🧠 Emotion: $emotion (Confidence: %.2f)".format(confidence),
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@CheckInActivity,
                                    "❌ Emotion detection failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<List<Map<String, Any>>>, t: Throwable) {
                            Toast.makeText(
                                this@CheckInActivity,
                                "API error: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }

            // 8. Reset
            moodSeekBar.progress = 2
            notesEditText.text.clear()
            clearChipSelections()
        }
    }

    private fun getSelectedTags(): List<String> {
        val tags = mutableListOf<String>()
        for (i in 0 until tagChipGroup.childCount) {
            val chip = tagChipGroup.getChildAt(i) as? Chip
            if (chip?.isChecked == true) {
                tags.add(chip.text.toString())
            }
        }
        return tags
    }

    private fun clearChipSelections() {
        for (i in 0 until tagChipGroup.childCount) {
            val chip = tagChipGroup.getChildAt(i) as? Chip
            chip?.isChecked = false
        }
    }
}
