package com.dokugo.ui.moneymanage

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dokugo.R
import com.dokugo.database.IncomeDatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

class IncomeFragment : Fragment() {

    private lateinit var etDatePicker: EditText
    private lateinit var etAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etNote: EditText
    private lateinit var btnSave: Button

    private val calendar = Calendar.getInstance()

    private val categories = arrayOf("Salary", "Others")

    private lateinit var dbHelper: IncomeDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_income, container, false)

        // Initialize views
        etDatePicker = rootView.findViewById(R.id.etDatePicker)
        etAmount = rootView.findViewById(R.id.etAmount)
        spinnerCategory = rootView.findViewById(R.id.spinnerCategory)
        etNote = rootView.findViewById(R.id.etNote)
        btnSave = rootView.findViewById(R.id.btnSave)

        // Initialize the database helper
        dbHelper = IncomeDatabaseHelper(requireContext())

        // Set up the Date Picker
        etDatePicker.setOnClickListener {
            showDatePickerDialog()
        }

        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, categories) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(ContextCompat.getColor(context, R.color.black)) // Mengubah warna teks spinner
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(ContextCompat.getColor(context, R.color.black)) // Mengubah warna teks dropdown
                return view
            }
        }

// Menetapkan adapter ke spinner
        spinnerCategory.adapter = adapter


        // Set up the Save button
        btnSave.setOnClickListener {
            saveIncomeData()
        }

        return rootView
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                etDatePicker.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun saveIncomeData() {
        // Retrieve data from inputs
        val date = etDatePicker.text.toString()
        val amount = etAmount.text.toString()
        val category = spinnerCategory.selectedItem.toString()
        val note = etNote.text.toString()

        // Validate inputs
        if (date.isEmpty() || amount.isEmpty() || category.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all the fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // Save data into the database
        val rowId = dbHelper.insertIncome(date, amount, category, note)

        if (rowId != -1L) {
            Toast.makeText(requireContext(), "Income saved successfully!", Toast.LENGTH_SHORT).show()

            // Navigate back to MoneyFragment
            findNavController().navigate(R.id.action_incomeFragment_to_moneyFragment)
        } else {
            Toast.makeText(requireContext(), "Failed to save income. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
}
