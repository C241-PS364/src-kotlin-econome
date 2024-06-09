package com.dicoding.econome.activity

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.dicoding.econome.R
import com.dicoding.econome.database.AppDatabase
import com.dicoding.econome.database.entity.Transaction
import com.dicoding.econome.databinding.ActivityAddIncomeBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Calendar

class AddIncomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddIncomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if the device version is greater than or equal to Lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Change the status bar color
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        }

        binding.amountInput.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        binding.labelInput.doOnTextChanged { text, _, _, _ ->
            if (text != null && text.count() > 0)
                binding.labelLayout.error = null
        }

        binding.amountInput.doOnTextChanged { text, _, _, _ ->
            if (text != null && text.count() > 0)
                binding.amountLayout.error = null
        }

        binding.dateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear)
                binding.dateButton.text = selectedDate
            }, year, month, day).show()
        }

        binding.addIncomeButton.setOnClickListener {
            val label = binding.labelInput.text.toString()
            val amount = binding.amountInput.text.toString().toDoubleOrNull()
            val date = binding.dateButton.text.toString()

            if (label.isEmpty()) {
                Toast.makeText(this, "Label cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (amount == null) {
                binding.amountLayout.error = "Please enter a valid amount"
            } else if (date == "Select Date") {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            } else {
                val transaction = Transaction(0, label, amount, date = date)
                insert(transaction)
            }
        }

        binding.closeButton.setOnClickListener {
            finish()
        }
    }

    private fun insert(transaction: Transaction) {
        val db = AppDatabase.getDatabase(this)

        GlobalScope.launch {
            db.transactionDao().insertAll(transaction)
            finish()
        }
    }
}