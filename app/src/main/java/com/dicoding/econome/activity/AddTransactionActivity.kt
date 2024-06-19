package com.dicoding.econome.activity

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.dicoding.econome.R
import com.dicoding.econome.activity.expense.ExpenseResponses
import com.dicoding.econome.auth.ApiConfig
import com.dicoding.econome.database.AppDatabase
import com.dicoding.econome.database.entity.Transaction
import com.dicoding.econome.databinding.ActivityAddTransactionBinding
import com.dicoding.econome.expense.ExpenseRepository
import com.dicoding.econome.expense.ExpenseRequests
import com.dicoding.econome.income.IncomeRepository
import com.dicoding.econome.income.IncomeRequests
import com.dicoding.econome.income.IncomeResponses
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
            "Health",
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
                    String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
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
                var transaction = Transaction(0, label, amount, category, date = date)

                if (isIncome) {
                    // If the transaction is an income, make the API call first
                    val sharedPreferences = getApplicationContext().getSharedPreferences(
                        "UserData",
                        Context.MODE_PRIVATE
                    )
                    val token = sharedPreferences.getString("token", null)
                    if (token != null) {
                        val incomeService = ApiConfig.incomeService
                        val database = AppDatabase.getDatabase(this)
                        val incomeRepository = IncomeRepository(incomeService, database)
                        val request = IncomeRequests.AddIncomeRequest(date, label, amount.toInt())
                        incomeRepository.addIncome(
                            "Bearer $token",
                            request
                        ) { response: IncomeResponses.AddIncomeResponse?, error: String? ->
                            if (response != null) {
                                // If the API call is successful, create a new Transaction object with the incomeId and insert it into the local database
                                transaction = Transaction(
                                    0,
                                    label,
                                    amount,
                                    category,
                                    date = date,
                                    incomeId = response.data.id
                                )
                                insert(transaction)
                            } else {
                                Log.d("Income", "Failed to add income: $error")
                                Toast.makeText(this, "Failed to add income", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    } else {
                        Log.d("Income", "Token is null")
                        Toast.makeText(this, "Token is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // If the transaction is an expense, make the API call first
                    val sharedPreferences = getApplicationContext().getSharedPreferences(
                        "UserData",
                        Context.MODE_PRIVATE
                    )
                    val token = sharedPreferences.getString("token", null)
                    if (token != null) {
                        val expenseService = ApiConfig.expenseService
                        val database = AppDatabase.getDatabase(this)
                        val expenseRepository = ExpenseRepository(expenseService, database)

                        val categories = mapOf(
                            "Food" to 1,
                            "Housing" to 2,
                            "Entertainment" to 3,
                            "Health" to 13,
                            "Transportation" to 14,
                            "Other" to 15
                        )

                        val categoryId = categories[category]
                            ?: 0 // Use 0 as default if the category is not found in the map
                        val request = ExpenseRequests.AddExpenseRequest(
                            date,
                            label,
                            categoryId,
                            amount.toInt()
                        ) // Assuming category is an integer ID
                        expenseRepository.addExpense(
                            "Bearer $token",
                            request
                        ) { response: ExpenseResponses.AddExpenseResponse?, error: String? ->
                            if (response != null) {
                                // If the API call is successful, create a new Transaction object with the expenseId and insert it into the local database
                                val expenseId = response.data.id
                                transaction = Transaction(
                                    0,
                                    label,
                                    amount,
                                    category,
                                    date = date,
                                    expenseId = expenseId
                                )
                                insert(transaction)
                            } else {
                                Log.d("Expense", "Failed to add expense: $error")
                                Toast.makeText(this, "Failed to add expense", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    } else {
                        Log.d("Expense", "Token is null")
                        Toast.makeText(this, "Token is null", Toast.LENGTH_SHORT).show()
                    }
                }
                val intent = Intent(this, TransactionActivity::class.java)
                intent.putExtra("IS_INCOME", isIncome)
                startActivity(intent)
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