package com.dicoding.econome.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.dicoding.econome.R
import com.dicoding.econome.database.AppDatabase
import com.dicoding.econome.database.entity.Transaction
import com.dicoding.econome.databinding.ActivityAddTransactionBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Calendar

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private var isIncome: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        val categories = arrayOf(
            "Entertainment",
            "Food",
            "Health and Beauty",
            "Housing",
            "Transportation",
            "Other"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.categoryInput.setAdapter(adapter)


        binding.toggleButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            isIncome = checkedId == R.id.incomeButton
            updateUI()
        }
        binding.toggleButton.isSingleSelection = true
        binding.toggleButton.check(R.id.expenseButton)
        binding.expenseButton.setOnClickListener {
            isIncome = false
            updateUI()
        }

        binding.incomeButton.setOnClickListener {
            isIncome = true
            updateUI()
        }

        binding.dateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate =
                    String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear)
                binding.dateButton.text = selectedDate
            }, year, month, day).show()
        }

        binding.addTransactionButton.setOnClickListener {
            val label = binding.labelInput.text.toString()
            var amount = binding.amountInput.text.toString().toDoubleOrNull()
            val category = if (isIncome) "" else binding.categoryInput.text.toString()
            val date = binding.dateButton.text.toString()

            if (label.isEmpty())
                Toast.makeText(this, "Label cannot be empty", Toast.LENGTH_SHORT).show()
            else if (!isIncome && category.isEmpty())
                Toast.makeText(this, "Category cannot be empty", Toast.LENGTH_SHORT).show()
            else if (!isIncome && !categories.contains(category))
                Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show()
            else if (amount == null)
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            else if (date == "Select Date")
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            else {
                if (!isIncome) {
                    amount = -Math.abs(amount!!)
                }
                val transaction = Transaction(0, label, amount, category, date = date)
                insert(transaction)
            }
        }
        binding.closeButton.setOnClickListener {
            finish()
        }
    }

    private fun updateUI() {
        if (isIncome) {
            binding.categoryLayout.visibility = View.GONE
        } else {
            binding.categoryLayout.visibility = View.VISIBLE
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