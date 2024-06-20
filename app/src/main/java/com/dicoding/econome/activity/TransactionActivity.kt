package com.dicoding.econome.activity

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.dicoding.econome.R
import com.dicoding.econome.activity.expense.ExpenseResponses
import com.dicoding.econome.adapter.TransactionAdapter
import com.dicoding.econome.auth.ApiConfig
import com.dicoding.econome.database.AppDatabase
import com.dicoding.econome.database.entity.Transaction
import com.dicoding.econome.databinding.ActivityTransactionBinding
import com.dicoding.econome.expense.ExpenseRepository
import com.dicoding.econome.income.IncomeRepository
import com.dicoding.econome.income.IncomeResponses
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class TransactionActivity : AppCompatActivity() {
    private lateinit var deleteTransaction: Transaction
    private lateinit var oldTransactions: List<Transaction>
    private lateinit var binding: ActivityTransactionBinding
    private lateinit var transactions: List<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var db: AppDatabase
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoTransaction: TextView

    private var selectedTimeRange = "All Time"
    private var selectedCategory = "All Categories"
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTransactionBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        tvNoTransaction = findViewById(R.id.tvNoTransaction)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        val menu = bottomNavigationView.menu
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            val spannableString = SpannableString(menuItem.title)
            val end = spannableString.length
            spannableString.setSpan(
                RelativeSizeSpan(0.8f),
                0,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            menuItem.title = spannableString
        }

        binding.bottomNavigationView.selectedItemId = R.id.miWallet

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val intent = when (item.itemId) {
                R.id.miHome -> Intent(this, MainActivity::class.java)
                R.id.miWallet -> Intent(this, TransactionActivity::class.java)
                R.id.miStatistics -> Intent(this, StatisticsActivity::class.java)
                R.id.miProfile -> Intent(this, ProfileActivity::class.java)
                else -> null
            }

            intent?.let {
                val options = ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle()
                startActivity(it, options)
            }

            true
        }
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(2).isEnabled = false
        binding.bottomNavigationView.itemIconTintList =
            ContextCompat.getColorStateList(this, R.color.bottom_nav_item_color)
        binding.bottomNavigationView.itemTextColor =
            ContextCompat.getColorStateList(this, R.color.bottom_nav_item_color)

        val isIncome = intent.getBooleanExtra("IS_INCOME", false)
        binding.addTransactionFAB.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("IS_INCOME", isIncome)
            startActivity(intent)
        }

        transactions = arrayListOf()

        transactionAdapter = TransactionAdapter(transactions)
        linearLayoutManager = LinearLayoutManager(this)

        db = Room.databaseBuilder(this, AppDatabase::class.java, "transactions")
            .build()

        binding.rvTransactions.apply {
            adapter = transactionAdapter
            layoutManager = this@TransactionActivity.linearLayoutManager
        }

        val itemTouchHelper = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                target: androidx.recyclerview.widget.RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(
                viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                direction: Int
            ) {
                deleteTransaction(transactions[viewHolder.adapterPosition])
            }

        }

        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(binding.rvTransactions)

        // Initialize the spinners
        val spinnerTimeRange: Spinner = findViewById(R.id.spinnerTimeRange)
        val spinnerCategory: Spinner = findViewById(R.id.spinnerCategory)

        ArrayAdapter.createFromResource(
            this,
            R.array.time_range,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_item)
            spinnerTimeRange.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.categories,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_item)
            spinnerCategory.adapter = adapter
        }

        spinnerTimeRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedTimeRange = parent.getItemAtPosition(position).toString()
                // Update your RecyclerView based on the selected time range
                fetchFiltered(selectedTimeRange, selectedCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCategory = parent.getItemAtPosition(position).toString()
                // Update your RecyclerView based on the selected category
                fetchFiltered(selectedTimeRange, selectedCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }

        binding.backButton.setOnClickListener {
            binding.bottomNavigationView.selectedItemId = R.id.miHome
            finish()
        }

        progressBar = findViewById(R.id.progressBarRvTransactions)
        fetchFiltered(selectedTimeRange, selectedCategory)
    }


    private fun fetchAll() {
        GlobalScope.launch {
            transactions = db.transactionDao().getAll()

            runOnUiThread {
                transactionAdapter.setData(transactions)
            }
        }
    }

    private fun fetchFiltered(timeRange: String, category: String) {
        progressBar.visibility = View.VISIBLE

        GlobalScope.launch {
            delay(300)

            val allTransactions = db.transactionDao().getAll()

            // Get the current date
            val currentDate = LocalDate.now()

            // Filter transactions based on the selected time range
            val filteredByTimeRange = when (timeRange) {
                "All Time" -> allTransactions
                "Last 7 Days" -> allTransactions.filter {
                    val transactionDate =
                        LocalDate.parse(it.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    ChronoUnit.DAYS.between(transactionDate, currentDate) <= 7
                }

                "Last 30 Days" -> allTransactions.filter {
                    val transactionDate =
                        LocalDate.parse(it.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    ChronoUnit.DAYS.between(transactionDate, currentDate) <= 30
                }

                else -> allTransactions
            }

            // Filter transactions based on the selected category
            val filteredTransactions = if (category == "All Categories") {
                filteredByTimeRange
            } else {
                filteredByTimeRange.filter { it.category == category }
            }

            transactions = filteredTransactions

            runOnUiThread {
                transactionAdapter.setData(transactions)
                if (transactions.isEmpty()) {
                    tvNoTransaction.visibility = View.VISIBLE
                    binding.rvTransactions.visibility = View.GONE
                } else {
                    tvNoTransaction.visibility = View.GONE
                    binding.rvTransactions.visibility = View.VISIBLE
                }
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun undoDelete() {
        GlobalScope.launch {
            db.transactionDao().insertAll(deleteTransaction)
            transactions = oldTransactions
            runOnUiThread {
                transactionAdapter.setData(transactions)
            }
        }
    }

    private fun showSnackbar() {
        val view = findViewById<View>(R.id.main)
        val snackbar = Snackbar.make(view, "Transaction deleted", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo") {
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this, R.color.red))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .setAnchorView(binding.addTransactionFAB)
            .show()
    }

    private fun deleteTransaction(transaction: Transaction) {
        deleteTransaction = transaction
        oldTransactions = transactions

        GlobalScope.launch {
            db.transactionDao().delete(transaction)

            // Delete the income from the server
            if (transaction.amount > 0 && transaction.incomeId != null) { // Check if the transaction is an income and incomeId is not null
                val sharedPreferences =
                    getApplicationContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("token", null)
                if (token != null) {
                    val incomeService = ApiConfig.incomeService
                    val database = AppDatabase.getDatabase(this@TransactionActivity)
                    val incomeRepository = IncomeRepository(incomeService, database)
                    incomeRepository.deleteIncome(
                        "Bearer $token",
                        transaction.incomeId
                    ) { response: IncomeResponses.DeleteIncomeResponse?, error: String? ->
                        if (response != null) {
                            Log.d("Income", "Income deleted successfully: ${response.message}")
                        } else {
                            Log.d("Income", "Failed to delete income: $error")
                        }
                    }
                } else {
                    Log.d("Income", "Token is null")
                }
            }

            // Delete the expense from the server
            if (transaction.amount < 0 && transaction.expenseId != null) { // Check if the transaction is an expense and expenseId is not null
                val sharedPreferences =
                    getApplicationContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("token", null)
                if (token != null) {
                    val expenseService = ApiConfig.expenseService
                    val database = AppDatabase.getDatabase(this@TransactionActivity)
                    val expenseRepository = ExpenseRepository(expenseService, database)
                    expenseRepository.deleteExpense(
                        "Bearer $token",
                        transaction.expenseId
                    ) { response: ExpenseResponses.DeleteExpenseResponse?, error: String? ->
                        if (response != null) {
                            Log.d("Expense", "Expense deleted successfully: ${response.message}")
                        } else {
                            Log.d("Expense", "Failed to delete expense: $error")
                        }
                    }
                } else {
                    Log.d("Expense", "Token is null")
                }
            }

            transactions = transactions.filter { it.id != transaction.id }
            runOnUiThread {
                transactionAdapter.setData(transactions)
                showSnackbar()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchFiltered(selectedTimeRange, selectedCategory)
    }
}