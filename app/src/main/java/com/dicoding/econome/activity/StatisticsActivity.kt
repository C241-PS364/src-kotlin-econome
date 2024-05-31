package com.dicoding.econome.activity

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.econome.R
import com.dicoding.econome.database.AppDatabase
import com.dicoding.econome.database.entity.Transaction
import com.dicoding.econome.databinding.ActivityStatisticsBinding
import com.dicoding.econome.model.TopSpending
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class StatisticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatisticsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup bottom navigation
        setupBottomNavigation()

        // Setup RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val topSpendings = mutableListOf<TopSpending>()
        val adapter = TopSpendingAdapter(topSpendings)
        binding.recyclerView.adapter = adapter

        // Fetch transactions and setup pie chart
        CoroutineScope(Dispatchers.IO).launch {
            val transactions = getTransactionsFromDatabase()
            val expenseTransactions = transactions.filter { it.amount < 0 } // Filter only expenses
            val categorySums = expenseTransactions.groupBy { it.category }
                .mapValues { (_, trans) -> trans.sumOf { it.amount } }
            val categoryCounts = expenseTransactions.groupBy { it.category }
                .mapValues { (_, trans) -> trans.size }

            // Define the order of categories
            val categoriesOrder = listOf("Food", "Other", "Health and Beauty", "Transportation", "Housing", "Entertainment")

            // Sort the entries according to the defined order
            val pieEntries = categoriesOrder.mapNotNull { category ->
                categorySums[category]?.let { PieEntry(abs(it.toFloat()), category) }
            }

            val pieDataSet = PieDataSet(pieEntries, "Expenses")

            // Set colors for each category
            val categoryColors = mapOf(
                "Food" to R.color.colorFood,
                "Other" to R.color.colorOther,
                "Health and Beauty" to R.color.colorHealth,
                "Transportation" to R.color.colorTransportation,
                "Housing" to R.color.colorHousing,
                "Entertainment" to R.color.colorEntertainment
            )
            pieDataSet.colors = categoriesOrder.map { category ->
                ContextCompat.getColor(this@StatisticsActivity, categoryColors[category] ?: R.color.colorOther)
            }
            pieDataSet.valueTextColor = Color.BLACK
            pieDataSet.valueTextSize = 12f
            pieDataSet.valueFormatter = PercentFormatter(binding.pieChart)

            val pieData = PieData(pieDataSet)

            withContext(Dispatchers.Main) {
                binding.pieChart.data = pieData
                binding.pieChart.description.isEnabled = false
                binding.pieChart.isDrawHoleEnabled = true
                binding.pieChart.setHoleColor(Color.GRAY)
                binding.pieChart.setTransparentCircleColor(Color.GRAY)
                binding.pieChart.setDrawEntryLabels(false)
                binding.pieChart.setUsePercentValues(true)
                binding.pieChart.invalidate()

                // Update RecyclerView data
                topSpendings.clear()
                topSpendings.addAll(categoriesOrder.mapNotNull { category ->
                    categorySums[category]?.let { totalExpense ->
                        categoryCounts[category]?.let { itemCount ->
                            TopSpending(
                                iconRes = getCategoryIconRes(category),
                                category = category,
                                itemCount = itemCount,
                                totalExpense = abs(totalExpense).toFloat()
                            )
                        }
                    }
                })
                adapter.notifyDataSetChanged()
            }
        }

        binding.addTransactionFAB.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getCategoryIconRes(category: String): Int {
        return when (category) {
            "Food" -> R.drawable.ic_food
            "Other" -> R.drawable.ic_other
            "Health and Beauty" -> R.drawable.ic_health
            "Transportation" -> R.drawable.ic_transportation
            "Housing" -> R.drawable.ic_housing
            "Entertainment" -> R.drawable.ic_entertainment
            else -> R.drawable.ic_other
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.selectedItemId = R.id.miStatistics

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
    }

    private suspend fun getTransactionsFromDatabase(): List<Transaction> {
        return withContext(Dispatchers.IO) {
            val dao = AppDatabase.getDatabase(this@StatisticsActivity).transactionDao()
            dao.getAll()
        }
    }
}
