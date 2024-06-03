package com.dicoding.econome.activity

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.econome.adapter.TransactionAdapter
import com.dicoding.econome.database.AppDatabase
import com.dicoding.econome.database.entity.Transaction
import com.dicoding.econome.databinding.ActivityTopSpendingDetailsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class TopSpendingDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTopSpendingDetailsBinding
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopSpendingDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val category = intent.getStringExtra("CATEGORY") ?: ""
        val timeRange = intent.getStringExtra("TIME_RANGE") ?: ""

        val spinnerTimeRange = binding.spinnerTimeRange
        val timeRanges = arrayOf("All Time", "Last 7 Days", "Last 30 Days")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeRanges)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTimeRange.adapter = adapter

        spinnerTimeRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedTimeRange = timeRanges[position]
                CoroutineScope(Dispatchers.IO).launch {
                    val transactions = getTransactionsFromDatabase(category, selectedTimeRange)
                    withContext(Dispatchers.Main) {
                        setupRecyclerView(transactions)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun setupRecyclerView(transactions: List<Transaction>) {
        adapter = TransactionAdapter(transactions)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private suspend fun getTransactionsFromDatabase(category: String, timeRange: String): List<Transaction> {
        val dao = AppDatabase.getDatabase(this).transactionDao()
        val allTransactions = dao.getAll()

        // Get the current date
        val currentDate = LocalDate.now()

        // Filter transactions based on the selected time range
        val filteredByTimeRange = when (timeRange) {
            "All Time" -> allTransactions
            "Last 7 Days" -> allTransactions.filter {
                val transactionDate = LocalDate.parse(it.date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                ChronoUnit.DAYS.between(transactionDate, currentDate) <= 7
            }
            "Last 30 Days" -> allTransactions.filter {
                val transactionDate = LocalDate.parse(it.date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                ChronoUnit.DAYS.between(transactionDate, currentDate) <= 30
            }
            else -> allTransactions
        }

        // Filter transactions based on the selected category
        return if (category == "All Categories") {
            filteredByTimeRange
        } else {
            filteredByTimeRange.filter { it.category == category }
        }
    }
}