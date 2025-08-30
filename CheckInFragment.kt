package com.example.mindspace.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mindspace.R
import com.example.mindspace.data.CheckIn
import com.example.mindspace.data.CheckInDatabase
import com.example.mindspace.util.EmergencyUtils
import com.example.mindspace.util.SecureStorageManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CheckInFragment : Fragment() {

    private var emergencyContact: String? = null
    private var detectedCodeWord: String? = null

    // ✅ Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted && emergencyContact != null && detectedCodeWord != null) {
            EmergencyUtils.sendEmergencySms(requireContext(), emergencyContact!!, detectedCodeWord!!)
        } else {
            Toast.makeText(requireContext(), "Permission denied. Cannot send alert.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_check_in, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val seekBar = view.findViewById<SeekBar>(R.id.moodSeekBar)
        val moodValue = view.findViewById<TextView>(R.id.moodValue)
        val moodEmoji = view.findViewById<TextView>(R.id.moodEmoji)
        val notes = view.findViewById<EditText>(R.id.extraNotes)
        val chipGroup = view.findViewById<ChipGroup>(R.id.tagChipGroup)
        val saveButton = view.findViewById<Button>(R.id.saveCheckIn)

        val moodEmojis = listOf("😢", "😟", "😐", "😊", "😁")
        seekBar.progress = 2
        moodEmoji.text = moodEmojis[2]
        moodValue.text = "Mood: 3"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                moodEmoji.text = moodEmojis.getOrNull(progress) ?: ""
                moodValue.text = "Mood: ${progress + 1}"
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        saveButton.setOnClickListener {
            val moodScore = seekBar.progress + 1
            val notesText = notes.text.toString()

            val selectedTags = (0 until chipGroup.childCount).mapNotNull { i ->
                val chip = chipGroup.getChildAt(i)
                if (chip is Chip && chip.isChecked) chip.text.toString() else null
            }

            val checkIn = CheckIn(
                mood = moodScore,
                notes = notesText,
                tags = selectedTags.joinToString(", "),
                timestamp = System.currentTimeMillis()
            )

            // 🔐 Emergency code word detection
            val codeWord = SecureStorageManager.getCodeWord(requireContext()) ?: ""
            emergencyContact = SecureStorageManager.getEmergencyContact(requireContext())
            detectedCodeWord = codeWord

            if (notesText.contains(codeWord, ignoreCase = true) && emergencyContact != null) {
                showEmergencyAlertDialog(emergencyContact!!, codeWord)
            }

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    CheckInDatabase.getDatabase(requireContext())
                        .checkInDao()
                        .insert(checkIn)
                }

                Toast.makeText(requireContext(), "Check-in saved!", Toast.LENGTH_SHORT).show()
                seekBar.progress = 2
                notes.setText("")
                for (i in 0 until chipGroup.childCount) {
                    val chip = chipGroup.getChildAt(i)
                    if (chip is Chip) chip.isChecked = false
                }

                findNavController().navigate(R.id.action_checkInFragment_to_historyFragment)
            }
        }
    }

    private fun showEmergencyAlertDialog(contact: String, codeWord: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("🚨 Emergency Code Detected")
            .setMessage("Your secret code was detected. Alert $contact?")
            .setPositiveButton("Send WhatsApp") { _, _ ->
                EmergencyUtils.sendWhatsAppMessage(requireContext(), contact, codeWord)
            }
            .setNeutralButton("Send SMS") { _, _ ->
                checkPermissionsAndSendSms()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkPermissionsAndSendSms() {
        val permissionsNeeded = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.SEND_SMS
        )

        val notGranted = permissionsNeeded.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            EmergencyUtils.sendEmergencySms(requireContext(), emergencyContact!!, detectedCodeWord!!)
        } else {
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }
}
