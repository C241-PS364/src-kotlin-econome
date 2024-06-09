package com.dicoding.econome.activity

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.econome.R
import com.dicoding.econome.adapter.TransactionAdapter
import com.dicoding.econome.database.AppDatabase
import com.dicoding.econome.database.entity.Transaction
import com.dicoding.econome.databinding.ActivityTopSpendingDetailsBinding
import com.google.android.material.tabs.TabLayout
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

        // Check if the device version is greater than or equal to Lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Change the status bar color
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        }

        val category = intent.getStringExtra("CATEGORY") ?: ""

        // Initialize the TabLayout
        val tabLayoutTimeRange: TabLayout = binding.tabLayoutTimeRange
        val timeRanges = arrayOf("All Time", "Last 7 Days", "Last 30 Days")
        timeRanges.forEach { timeRange ->
            tabLayoutTimeRange.addTab(tabLayoutTimeRange.newTab().setText(timeRange))
        }

        // Set the default selected tab to "All Time"
        tabLayoutTimeRange.getTabAt(0)?.select()

        val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val selectedTimeRange = timeRanges[tab.position]
                CoroutineScope(Dispatchers.IO).launch {
                    val transactions = getTransactionsFromDatabase(category, selectedTimeRange)
                    withContext(Dispatchers.Main) {
                        setupRecyclerView(transactions)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Do nothing
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Do nothing
            }
        }

        tabLayoutTimeRange.addOnTabSelectedListener(tabSelectedListener)

        // Manually call onTabSelected for the default tab
        tabSelectedListener.onTabSelected(tabLayoutTimeRange.getTabAt(0)!!)

        binding.backButton.setOnClickListener {
            finish()
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