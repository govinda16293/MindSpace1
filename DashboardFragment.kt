package com.example.mindspace

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mindspace.data.CheckIn
import com.example.mindspace.data.CheckInDatabase
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardFragment : Fragment() {

    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lineChart = view.findViewById(R.id.lineChartMood)
        pieChart = view.findViewById(R.id.pieChartTags)

        lifecycleScope.launch {
            val checkIns = withContext(Dispatchers.IO) {
                CheckInDatabase.getDatabase(requireContext())
                    .checkInDao()
                    .getAllCheckInsList()
            }

            if (checkIns.isNotEmpty()) {
                showMoodLineChart(checkIns)
                showTagPieChart(checkIns)
            } else {
                lineChart.clear()
                pieChart.clear()
                lineChart.setNoDataText("No mood data available.")
                pieChart.setNoDataText("No tag data available.")
            }
        }
    }

    private fun showMoodLineChart(checkIns: List<CheckIn>) {
        val entries = checkIns.mapIndexed { index, checkIn ->
            Entry(index.toFloat(), checkIn.mood.toFloat())
        }

        val lineDataSet = LineDataSet(entries, "Mood Score").apply {
            color = Color.BLUE
            circleColors = listOf(Color.BLUE)
            valueTextColor = Color.BLACK
            lineWidth = 2f
            circleRadius = 4f
        }

        lineChart.apply {
            data = LineData(lineDataSet)
            description = Description().apply { text = "Mood Over Time" }
            setTouchEnabled(true)
            setPinchZoom(true)
            animateX(1000)
            invalidate()
        }
    }

    private fun showTagPieChart(checkIns: List<CheckIn>) {
        val tagFrequency = mutableMapOf<String, Int>()

        for (checkIn in checkIns) {
            val tags = checkIn.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
            for (tag in tags) {
                tagFrequency[tag] = tagFrequency.getOrDefault(tag, 0) + 1
            }
        }

        val entries = tagFrequency.map { (tag, count) ->
            PieEntry(count.toFloat(), tag)
        }

        val dataSet = PieDataSet(entries, "Mood Tags").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextColor = Color.BLACK
            valueTextSize = 14f
        }

        pieChart.apply {
            data = PieData(dataSet)
            description = Description().apply { text = "Tag Frequency" }
            setUsePercentValues(true)
            animateY(1000)
            invalidate()
        }
    }
}
