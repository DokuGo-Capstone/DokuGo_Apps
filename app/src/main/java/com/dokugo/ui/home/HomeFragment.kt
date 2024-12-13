package com.dokugo.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dokugo.R
import com.dokugo.database.ExpenseDatabaseHelper
import com.dokugo.database.IncomeDatabaseHelper
import com.dokugo.databinding.FragmentHomeBinding
import com.dokugo.ui.custom.CustomButtonDate
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.WeekFields
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var btWeek: CustomButtonDate
    private lateinit var btMonth: CustomButtonDate
    private lateinit var btYear: CustomButtonDate
    private lateinit var btAll: CustomButtonDate
    private lateinit var tvDate: TextView
    private lateinit var incomeText: TextView
    private lateinit var expenseText: TextView
    private lateinit var lineChart: LineChart

    private lateinit var incomeDbHelper: IncomeDatabaseHelper
    private lateinit var expenseDbHelper: ExpenseDatabaseHelper

    // Decimal format for Indonesian currency (with period as thousands separator)
    private val currencyFormat = DecimalFormat("#,###,###", DecimalFormatSymbols(Locale("id", "ID")))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        incomeDbHelper = IncomeDatabaseHelper(requireContext())
        expenseDbHelper = ExpenseDatabaseHelper(requireContext())

        // Initialize the LineChart
        lineChart = binding.lineChart

        // Initialize the buttons and TextViews
        btWeek = binding.btWeek
        btMonth = binding.btMonth
        btYear = binding.btYear
        btAll = binding.btAll
        tvDate = binding.tvDate  // Get the reference to the TextView
        incomeText = binding.incomeText  // Get the reference to the income TextView
        expenseText = binding.centeredText  // Get the reference to the expense TextView

        // Set the default selected button (1W) as selected
        btWeek.setSelectedState(true)
        tvDate.text = "This Week"  // Default text

        // Initialize values for the default button (1W) at the start
        initializeValues()

        // Set up click listeners for the buttons
        btWeek.setOnClickListener { onButtonClicked(btWeek, "This Week") }
        btMonth.setOnClickListener { onButtonClicked(btMonth, "This Month") }
        btYear.setOnClickListener { onButtonClicked(btYear, "January - December") }
        btAll.setOnClickListener { onButtonClicked(btAll, "All Time") }

        return root
    }

    // Initialize values for the selected button (1W) at the start
    private fun initializeValues() {
        // Fetch income and expense data from the database
        val incomeData = fetchIncomeData("This Week")
        val expenseData = fetchExpenseData("This Week")

        // Calculate total income and expense
        val totalIncome = incomeData.sumOf { it.amount.toDouble() }
        val totalExpense = expenseData.sumOf { it.amount.toDouble() }

        // Update the TextViews with the values
        incomeText.text = "+Rp. ${currencyFormat.format(totalIncome.toLong())}"
        expenseText.text = "-Rp. ${currencyFormat.format(totalExpense.toLong())}"

        // Update the chart
        updateChartData("This Week")
    }

    // Update chart data with data from the database
    private fun updateChartData(period: String) {
        val incomeEntries = mutableListOf<Entry>()
        val expenseEntries = mutableListOf<Entry>()
        val xAxisLabels = mutableListOf<String>()

        val incomeData = fetchIncomeData(period)
        val expenseData = fetchExpenseData(period)

        // Populate entries and labels based on the period
        when (period) {
            "This Week" -> {
                // Add data points for the week
                val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
                for (i in 0..6) {
                    val date = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6 + i) }.time
                    xAxisLabels.add(dateFormat.format(date))
                    incomeEntries.add(Entry(i.toFloat(), incomeData.getOrNull(i)?.amount?.toFloat() ?: 0f))
                    expenseEntries.add(Entry(i.toFloat(), expenseData.getOrNull(i)?.amount?.toFloat() ?: 0f))
                }
            }
            "This Month" -> {
                // Add data points for the month
                val weekFields = WeekFields.of(Locale.getDefault())
                val weeks = arrayOf(0.0, 0.0, 0.0, 0.0)
                incomeData.forEach { income ->
                    val weekOfMonth = LocalDate.parse(income.date).get(weekFields.weekOfMonth()) - 1
                    weeks[weekOfMonth] += income.amount.toDouble()
                }
                weeks.forEachIndexed { index, amount ->
                    incomeEntries.add(Entry(index.toFloat(), amount.toFloat()))
                    xAxisLabels.add("Week ${index + 1}")
                }
                val expenseWeeks = arrayOf(0.0, 0.0, 0.0, 0.0)
                expenseData.forEach { expense ->
                    val weekOfMonth = LocalDate.parse(expense.date).get(weekFields.weekOfMonth()) - 1
                    expenseWeeks[weekOfMonth] += expense.amount.toDouble()
                }
                expenseWeeks.forEachIndexed { index, amount ->
                    expenseEntries.add(Entry(index.toFloat(), amount.toFloat()))
                }
            }
            "January - December" -> {
                // Add data points for the year
                for (i in 0..11) {
                    incomeEntries.add(Entry(i.toFloat(), incomeData.getOrNull(i)?.amount?.toFloat() ?: 0f))
                    expenseEntries.add(Entry(i.toFloat(), expenseData.getOrNull(i)?.amount?.toFloat() ?: 0f))
                    xAxisLabels.add(SimpleDateFormat("MMM", Locale.getDefault()).format(Calendar.getInstance().apply { set(Calendar.MONTH, i) }.time))
                }
            }
            "All Time" -> {
                // Add data points for all time
                for (i in 0..11) {
                    incomeEntries.add(Entry(i.toFloat(), incomeData.getOrNull(i)?.amount?.toFloat() ?: 0f))
                    expenseEntries.add(Entry(i.toFloat(), expenseData.getOrNull(i)?.amount?.toFloat() ?: 0f))
                    xAxisLabels.add((i + 1).toString())
                }
            }
        }

        // Create the datasets for Income and Expense
        val incomeDataSet = LineDataSet(incomeEntries, "Income")
        incomeDataSet.color = ContextCompat.getColor(requireContext(), R.color.income_chart)
        incomeDataSet.setDrawCircles(true)
        incomeDataSet.setDrawValues(false) // Hide values
        incomeDataSet.lineWidth = 5f

        val expenseDataSet = LineDataSet(expenseEntries, "Expense")
        expenseDataSet.color = ContextCompat.getColor(requireContext(), R.color.expense_chart)
        expenseDataSet.setDrawCircles(true)
        expenseDataSet.setDrawValues(false) // Hide values
        expenseDataSet.lineWidth = 5f

        // Create a list of datasets
        val dataSets: MutableList<ILineDataSet> = mutableListOf()
        if (incomeEntries.isNotEmpty()) {
            dataSets.add(incomeDataSet)
        }
        if (expenseEntries.isNotEmpty()) {
            dataSets.add(expenseDataSet)
        }

        // Create LineData and set it to the chart
        val lineData = LineData(dataSets)
        lineChart.data = lineData
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false

        // Customize chart axes
        setupChartAxes(xAxisLabels, incomeEntries, expenseEntries)

        // Refresh chart
        lineChart.invalidate()
    }

    // Setup chart axes
    private fun setupChartAxes(xAxisLabels: List<String>, incomeEntries: List<Entry>, expenseEntries: List<Entry>) {
        val xAxis: XAxis = lineChart.xAxis
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() in xAxisLabels.indices) {
                    xAxisLabels[value.toInt()]
                } else {
                    ""
                }
            }
        }
        xAxis.labelCount = xAxisLabels.size
        xAxis.isGranularityEnabled = true

        val yAxis: YAxis = lineChart.axisLeft
        yAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value == 0f) "0" else String.format(Locale("in", "ID"), "%.0f000", value / 1000)
            }
        }
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = (Math.ceil((maxOf(incomeEntries.maxOfOrNull { it.y } ?: 0f, expenseEntries.maxOfOrNull { it.y } ?: 0f).toDouble() / 1000) * 1000).toFloat() * 1.2f)
        yAxis.axisMinimum = 0f
        lineChart.axisRight.isEnabled = false
    }

    // Handle button click and update tv_date text
    private fun onButtonClicked(selectedButton: CustomButtonDate, buttonText: String) {
        // Reset the state for all buttons
        btWeek.setSelectedState(false)
        btMonth.setSelectedState(false)
        btYear.setSelectedState(false)
        btAll.setSelectedState(false)

        // Set the selected state for the clicked button
        selectedButton.setSelectedState(true)

        // Set the correct text for tv_date based on the button clicked
        when (buttonText) {
            "This Week" -> tvDate.text = "This Week"
            "This Month" -> tvDate.text = getCurrentMonth()
            "January - December" -> tvDate.text = "January - December"
            "All Time" -> tvDate.text = "All Time"
        }

        // Update income and expense with data from the database
        updateIncomeAndExpense(buttonText)

        // Update the chart with new data
        updateChartData(buttonText)
    }

    // Get the current month name (e.g., "March")
    private fun getCurrentMonth(): String {
        val currentDate = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        return monthFormat.format(currentDate.time)
    }

    // Update income and expense with data from the database
    private fun updateIncomeAndExpense(timeRange: String) {
        // Fetch income and expense data from the database
        val incomeData = fetchIncomeData(timeRange)
        val expenseData = fetchExpenseData(timeRange)

        // Calculate total income and expense
        val totalIncome = incomeData.sumOf { it.amount.toDouble() }
        val totalExpense = expenseData.sumOf { it.amount.toDouble() }

        // Update the TextViews with the values
        incomeText.text = "+Rp. ${currencyFormat.format(totalIncome.toLong())}"
        expenseText.text = "-Rp. ${currencyFormat.format(totalExpense.toLong())}"
    }

    // Function to fetch income data from the database
    private fun fetchIncomeData(timeRange: String): List<Income> {
        val db = incomeDbHelper.readableDatabase
        val cursor = db.query(
            IncomeDatabaseHelper.TABLE_NAME,
            null, null, null, null, null, null
        )

        val incomeList = mutableListOf<Income>()
        val currentDate = LocalDate.now()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(IncomeDatabaseHelper.COLUMN_ID))
                val date = getString(getColumnIndexOrThrow(IncomeDatabaseHelper.COLUMN_DATE))
                val amount = getString(getColumnIndexOrThrow(IncomeDatabaseHelper.COLUMN_AMOUNT))
                val incomeDate = LocalDate.parse(date)

                val isInRange = when (timeRange) {
                    "This Week" -> incomeDate.isAfter(currentDate.minusDays(7)) && incomeDate.isBefore(currentDate.plusDays(1))
                    "This Month" -> incomeDate.month == currentDate.month && incomeDate.year == currentDate.year
                    "January - December" -> incomeDate.year == currentDate.year
                    "All Time" -> true
                    else -> false
                }

                if (isInRange) {
                    incomeList.add(Income(id, date, amount))
                }
            }
        }
        cursor.close()
        return incomeList.sortedBy { LocalDate.parse(it.date) }
    }

    // Function to fetch expense data from the database
    private fun fetchExpenseData(timeRange: String): List<Expense> {
        val db = expenseDbHelper.readableDatabase
        val cursor = db.query(
            ExpenseDatabaseHelper.TABLE_NAME_EXPENSES,
            null, null, null, null, null, null
        )

        val expenseList = mutableListOf<Expense>()
        val currentDate = LocalDate.now()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(ExpenseDatabaseHelper.COLUMN_ID))
                val date = getString(getColumnIndexOrThrow(ExpenseDatabaseHelper.COLUMN_DATE))
                val amount = getString(getColumnIndexOrThrow(ExpenseDatabaseHelper.COLUMN_AMOUNT))
                val expenseDate = LocalDate.parse(date)

                val isInRange = when (timeRange) {
                    "This Week" -> expenseDate.isAfter(currentDate.minusDays(7)) && expenseDate.isBefore(currentDate.plusDays(1))
                    "This Month" -> expenseDate.month == currentDate.month && expenseDate.year == currentDate.year
                    "January - December" -> expenseDate.year == currentDate.year
                    "All Time" -> true
                    else -> false
                }

                if (isInRange) {
                    expenseList.add(Expense(id, date, amount))
                }
            }
        }
        cursor.close()
        return expenseList.sortedBy { LocalDate.parse(it.date) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Define the Income and Expense data classes
data class Income(val id: Int, val date: String, val amount: String)
data class Expense(val id: Int, val date: String, val amount: String)