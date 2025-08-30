package com.example.mindspace.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindspace.R
import com.example.mindspace.adapter.CheckInAdapter
import com.example.mindspace.data.CheckInDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private lateinit var adapter: CheckInAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_history, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.historyRecyclerView)
        emptyTextView = view.findViewById(R.id.emptyTextView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CheckInAdapter()
        recyclerView.adapter = adapter

        fetchCheckIns()
    }

    private fun fetchCheckIns() {
        lifecycleScope.launch {
            val checkIns = withContext(Dispatchers.IO) {
                CheckInDatabase.getDatabase(requireContext())
                    .checkInDao()
                    .getAllCheckInsList()  // ✅ fixed method name here
            }

            if (checkIns.isEmpty()) {
                emptyTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyTextView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.submitList(checkIns)
            }
        }
    }
}
