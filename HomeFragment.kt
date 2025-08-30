package com.example.mindspace

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import android.widget.Button

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        view.findViewById<Button>(R.id.btn_checkin).setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_checkInFragment)
        }

        view.findViewById<Button>(R.id.btn_journal).setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_journalFragment)
        }

        view.findViewById<Button>(R.id.btn_tools).setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_copingToolsFragment)
        }

        view.findViewById<Button>(R.id.btn_help).setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_helpFragment)
        }

        view.findViewById<Button>(R.id.btn_settings).setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_settingsFragment)
        }

        // ✅ New: Navigate to Camera-Based Emotion Detection
        view.findViewById<Button>(R.id.btnCameraEmotion).setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_cameraEmotionFragment)
        }
    }
}
