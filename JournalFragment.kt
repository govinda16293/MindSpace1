package com.example.mindspace

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindspace.adapter.JournalAdapter
import com.example.mindspace.data.JournalEntry
import com.example.mindspace.util.EmergencyUtils
import com.example.mindspace.util.SecureStorageManager
import com.example.mindspace.viewmodel.JournalViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class JournalFragment : Fragment() {

    private lateinit var viewModel: JournalViewModel
    private lateinit var adapter: JournalAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_journal, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val titleInput = view.findViewById<EditText>(R.id.journalTitle)
        val contentInput = view.findViewById<EditText>(R.id.journalContent)
        val saveButton = view.findViewById<MaterialButton>(R.id.btnSaveEntry)
        val recyclerView = view.findViewById<RecyclerView>(R.id.journalRecyclerView)

        viewModel = ViewModelProvider(this)[JournalViewModel::class.java]

        adapter = JournalAdapter { entryToDelete: JournalEntry ->
            viewModel.deleteEntry(entryToDelete)
            Toast.makeText(requireContext(), "Entry deleted", Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.allEntries.observe(viewLifecycleOwner) { entries ->
            adapter.submitList(entries)
        }

        saveButton.setOnClickListener {
            val title = titleInput.text.toString().takeIf { it.isNotBlank() }
            val content = contentInput.text.toString()

            if (content.isBlank()) {
                Toast.makeText(requireContext(), "Please write something!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val codeWord = SecureStorageManager.getCodeWord(requireContext()) ?: ""
            val emergencyContact = SecureStorageManager.getEmergencyContact(requireContext())

            if (content.contains(codeWord, ignoreCase = true) && emergencyContact != null) {
                showEmergencyAlertDialog(emergencyContact, codeWord)
            }

            viewModel.addEntryWithCallback(
                title = title,
                content = content,
                onRedFlagDetected = {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("⚠️ Emotional Alert")
                        .setMessage("This entry contains sensitive language. Would you like help?")
                        .setPositiveButton("Yes, Get Help") { _, _ ->
                            Toast.makeText(requireContext(), "Opening Help Screen...", Toast.LENGTH_SHORT).show()
                            // TODO: Navigate to HelpFragment if desired
                        }
                        .setNegativeButton("No, Just Save") { _, _ ->
                            saveJournal(titleInput, contentInput)
                        }
                        .show()
                },
                onSafeToSave = { emotion ->
                    val message = if (emotion != null)
                        "Journal saved\nDetected emotion: $emotion"
                    else
                        "Journal saved"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    titleInput.text.clear()
                    contentInput.text.clear()
                }
            )
        }
    }

    private fun saveJournal(titleInput: EditText, contentInput: EditText) {
        Toast.makeText(requireContext(), "Journal saved", Toast.LENGTH_SHORT).show()
        titleInput.text.clear()
        contentInput.text.clear()
    }

    private fun showEmergencyAlertDialog(contact: String, codeWord: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("🚨 Emergency Code Detected")
            .setMessage("Your secret code was detected in this journal. Alert $contact?")
            .setPositiveButton("Send WhatsApp") { _, _ ->
                EmergencyUtils.sendWhatsAppMessage(requireContext(), contact, codeWord)
            }
            .setNeutralButton("Send SMS") { _, _ ->
                EmergencyUtils.sendEmergencySms(requireContext(), contact, codeWord)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
