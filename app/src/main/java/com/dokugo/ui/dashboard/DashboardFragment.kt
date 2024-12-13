package com.dokugo.ui.dashboard

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.dokugo.database.ExpenseDatabaseHelper
import com.dokugo.prediction.PredictionHelper
import com.dokugo.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import java.util.Calendar
import java.text.SimpleDateFormat

class DashboardFragment : Fragment() {
    private lateinit var dbHelper: ExpenseDatabaseHelper
    private lateinit var predictionHelper: PredictionHelper
    private lateinit var lineChart: LineChart
    private lateinit var progressBar: View
    private lateinit var tvDate: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = ExpenseDatabaseHelper(requireContext())
        predictionHelper = PredictionHelper(requireContext())
        lineChart = view.findViewById(R.id.lineChart)
        progressBar = view.findViewById(R.id.progressBar)
        tvDate = view.findViewById(R.id.tv_date)

        val btWeek = view.findViewById<Button>(R.id.bt_week)
        val btMonth = view.findViewById<Button>(R.id.bt_month)
        val btYear = view.findViewById<Button>(R.id.bt_year)
        val btAll = view.findViewById<Button>(R.id.bt_all)

        val currentMonth = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())

        btWeek.setOnClickListener { onButtonClicked(btWeek, "This Week") }
        btMonth.setOnClickListener { onButtonClicked(btMonth, currentMonth) }
        btYear.setOnClickListener { onButtonClicked(btYear, "January - December") }
        btAll.setOnClickListener { onButtonClicked(btAll, "All Time") }

        // Set default button to "This Week"
        btWeek.isSelected = true
        tvDate.text = "This Week"
        updateChart("This Week")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onButtonClicked(selectedButton: Button, buttonText: String) {
        // Reset the state for all buttons
        view?.findViewById<Button>(R.id.bt_week)?.isSelected = false
        view?.findViewById<Button>(R.id.bt_month)?.isSelected = false
        view?.findViewById<Button>(R.id.bt_year)?.isSelected = false
        view?.findViewById<Button>(R.id.bt_all)?.isSelected = false

        // Set the selected state for the clicked button
        selectedButton.isSelected = true

        // Update the TextView text
        tvDate.text = buttonText

        // Update the chart based on the button clicked
        updateChart(buttonText)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateChart(timeRange: String) {
        progressBar.visibility = View.VISIBLE

        val expenses = when (timeRange) {
            "This Week" -> dbHelper.getExpensesForNext7Days()  // Fetch data for the next 7 days starting from today
            LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault()) -> dbHelper.getExpensesForLastMonth()
            "January - December" -> dbHelper.getExpensesForLastYear()
            else -> dbHelper.getHistoricalExpenses()
        }

        if (expenses.isNotEmpty()) {
            val predictions = mutableListOf<Float>()

            for (expense in expenses) {
                val prediction = predictionHelper.predict(listOf(expense))
                predictions.add(prediction)
            }

            setupLineChart(predictions, timeRange) // Pass the timeRange parameter here
            progressBar.visibility = View.GONE
        } else {
            Toast.makeText(context, "No expenses data found.", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }
    }

    private fun setupLineChart(predictions: List<Float>, timeRange: String) {
        val entries = mutableListOf<Entry>()

        // Add the prediction data points
        entries.addAll(predictions.mapIndexed { index, prediction ->
            Entry((index + 1).toFloat(), prediction)
        })

        val lineDataSet = LineDataSet(entries, "")
        lineDataSet.color = resources.getColor(R.color.expense_chart, null)
        lineDataSet.setDrawCircles(true)
        lineDataSet.setDrawValues(false)
        lineDataSet.lineWidth = 5f

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false

        // Customize XAxis based on the time range
        val xAxis: XAxis = lineChart.xAxis
        xAxis.valueFormatter = object : ValueFormatter() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt() - 1
                if (index < 0 || index >= predictions.size) {
                    return ""
                }
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, index) // Moving forward in time for each point
                return when (timeRange) {
                    "This Week" -> SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time) // Day of the week
                    LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault()) -> SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time) // Date
                    "January - December" -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(calendar.time) // Date and month
                    else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time) // Full date
                }
            }
        }
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelCount = predictions.size
        xAxis.isGranularityEnabled = true

        // Customize YAxis
        val leftAxis: YAxis = lineChart.axisLeft
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value == 0f) "0" else String.format(Locale("in", "ID"), "%.0f000", value / 1000)
            }
        }
        val maxPrediction = predictions.maxOrNull() ?: 0f
        leftAxis.axisMaximum = (Math.ceil(maxPrediction.toDouble() / 1000) * 1000 + 5000).toFloat() // Add buffer to max value
        leftAxis.axisMinimum = 0f
        lineChart.axisRight.isEnabled = false

        lineChart.invalidate()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        predictionHelper.close()
    }
}
