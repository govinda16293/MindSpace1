package com.example.mindspace

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

class HelpFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnCall = view.findViewById<Button>(R.id.btnCallHelpline)
        val btnCounselor = view.findViewById<Button>(R.id.btnFindCounselor)

        btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:18005990019")
            startActivity(intent)
        }

        btnCounselor.setOnClickListener {
            try {
                val gmmIntentUri = Uri.parse("geo:0,0?q=psychologist+near+me")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Google Maps not found.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
