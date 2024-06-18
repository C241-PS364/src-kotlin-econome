package com.dicoding.econome.activity

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.dicoding.econome.R
import com.dicoding.econome.auth.ApiConfig
import com.dicoding.econome.database.AppDatabase
import com.dicoding.econome.database.entity.Transaction
import com.dicoding.econome.databinding.ActivityMainBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private var transactions: List<Transaction> = listOf()

    private lateinit var timeRanges: Array<String>
    private var currentRangeIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        db = Room.databaseBuilder(this, AppDatabase::class.java, "transactions")
            .build()

        binding.bottomNavigationView.selectedItemId = R.id.miHome

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
        binding.addTransactionFAB.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        // Initialize the time ranges
        timeRanges = resources.getStringArray(R.array.time_range)

        // Setup the time range selector
        binding.tvTimeRange.text = timeRanges[currentRangeIndex]
        binding.btnPrevious.setOnClickListener {
            if (currentRangeIndex > 0) {
                currentRangeIndex--
                updateTimeRange()
            }
        }
        binding.btnNext.setOnClickListener {
            if (currentRangeIndex < timeRanges.size - 1) {
                currentRangeIndex++
                updateTimeRange()
            }
        }
        fetchUserProfile()
        fetchAll()

    }

    private fun fetchUserProfile() {
        // Ambil token dari SharedPreferences
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

        if (token != null) {
            val userService = ApiConfig.userService

            lifecycleScope.launch {
                val response = withContext(Dispatchers.IO) {
                    userService.getProfile("Bearer $token").execute()
                }
                if (response.isSuccessful) {
                    val profile = response.body()?.data
                    profile?.let {
                        updateGreeting(it.name)
                    }
                }
            }
        } else {
            // Handle case where token is not found
            updateGreeting("User")
        }
    }

    private fun updateGreeting(userName: String) {
        val greetingText = getString(R.string.greeting, userName)
        binding.tvGreeting.text = greetingText
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }

    private fun updateTimeRange() {
        binding.tvTimeRange.text = timeRanges[currentRangeIndex]
        val filteredTransactions = when (timeRanges[currentRangeIndex]) {
            "Last 7 Days" -> transactions.filter {
                val transactionDate =
                    LocalDate.parse(it.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                ChronoUnit.DAYS.between(transactionDate, LocalDate.now()) <= 7
            }

            "Last 30 Days" -> transactions.filter {
                val transactionDate =
                    LocalDate.parse(it.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                ChronoUnit.DAYS.between(transactionDate, LocalDate.now()) <= 30
            }

            else -> transactions
        }
        updateDashboard(filteredTransactions)
    }

    private fun updateChart(transactions: List<Transaction>) {
        val lineDataExpense = LineData()
        val lineDataIncome = LineData()

        // Define colors for each category
        val colors = mapOf(
            "Entertainment" to ContextCompat.getColor(this, R.color.colorEntertainment),
            "Food" to ContextCompat.getColor(this, R.color.colorFood),
            "Health" to ContextCompat.getColor(this, R.color.colorHealth),
            "Housing" to ContextCompat.getColor(this, R.color.colorHousing),
            "Other" to ContextCompat.getColor(this, R.color.colorOther),
            "Transportation" to ContextCompat.getColor(this, R.color.colorTransportation),
        )

        // Group transactions by category and date, then sum the amounts for each date
        val groupedTransactions = transactions.filter { it.amount < 0 }
            .groupBy { it.category }
            .mapValues { entry ->
                entry.value.groupBy { it.date }
                    .mapValues { it.value.sumOf { transaction -> transaction.amount } }
            }

        // Create a LineDataSet for each category
        groupedTransactions.forEach { (category, dateToAmountMap) ->
            val lineEntries = ArrayList<Entry>()
            dateToAmountMap.entries.forEachIndexed { index, entry ->
                val date = entry.key
                val amount = entry.value
                lineEntries.add(Entry(index.toFloat(), amount.toFloat()))
            }

            val lineDataSet = LineDataSet(lineEntries, category)
            lineDataSet.color = colors[category] ?: Color.BLACK
            lineDataSet.valueTextColor = Color.BLACK
            lineDataSet.valueTextSize = 16f
            lineDataSet.setDrawValues(false)

            lineDataExpense.addDataSet(lineDataSet)
        }

        binding.lineChart.data = lineDataExpense
        binding.lineChart.description.text = "Expenses over time"
        binding.lineChart.setNoDataText("No expenses yet!")
        binding.lineChart.axisLeft.setDrawGridLines(false)
        binding.lineChart.axisRight.setDrawGridLines(false)
        binding.lineChart.xAxis.setDrawGridLines(false)

        // invert the y-axis
        val yAxis = binding.lineChart.axisLeft
        yAxis.setInverted(true)

        val yAxisRight = binding.lineChart.axisRight
        yAxisRight.setDrawLabels(false)

        // Group transactions by category and date, then sum the amounts for each date
        val groupedIncomeTransactions = transactions.filter { it.amount > 0 }
            .groupBy { it.category }
            .mapValues { entry ->
                entry.value.groupBy { it.date }
                    .mapValues { it.value.sumOf { transaction -> transaction.amount } }
            }

        // Create a LineDataSet for each category
        groupedIncomeTransactions.forEach { (category, dateToAmountMap) ->
            val lineEntries = ArrayList<Entry>()
            dateToAmountMap.entries.forEachIndexed { index, entry ->
                val date = entry.key
                val amount = entry.value
                lineEntries.add(Entry(index.toFloat(), amount.toFloat()))
            }

            val lineDataSet = LineDataSet(lineEntries, category)
            lineDataSet.color = Color.GREEN // Use green color for income
            lineDataSet.valueTextColor = Color.BLACK
            lineDataSet.valueTextSize = 16f
            lineDataSet.setDrawValues(false)

            lineDataIncome.addDataSet(lineDataSet)
        }

        binding.lineChartIncome.data = lineDataIncome
        binding.lineChartIncome.description.text = "Income over time"
        binding.lineChartIncome.setNoDataText("No income yet!")
        binding.lineChartIncome.axisLeft.setDrawGridLines(false)
        binding.lineChartIncome.axisRight.setDrawGridLines(false)
        binding.lineChartIncome.xAxis.setDrawGridLines(false)

        val yAxisIncome = binding.lineChartIncome.axisLeft
        yAxisIncome.setInverted(false)

        val yAxisRightIncome = binding.lineChartIncome.axisRight
        yAxisRightIncome.setDrawLabels(false)

        // set date as x-axis labels
        val dates = transactions.map { it.date.substring(0, 5) }
            .distinct() // get only "dd-MM" and remove duplicates
        val xAxis = binding.lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(dates)
        xAxis.position = XAxis.XAxisPosition.BOTTOM // move dates to the bottom

        val xAxisIncome = binding.lineChartIncome.xAxis
        xAxisIncome.valueFormatter = IndexAxisValueFormatter(dates)
        xAxisIncome.position = XAxis.XAxisPosition.BOTTOM // move dates to the bottom

        binding.lineChartIncome.invalidate() // refreshes the chart
        binding.lineChart.invalidate() // refreshes the chart
    }

    private fun fetchAll() {
        GlobalScope.launch {
            transactions = db.transactionDao().getAll()

            runOnUiThread {
                updateDashboard(transactions)
            }
        }
    }

    private fun updateDashboard(filteredTransactions: List<Transaction>) {
        val balanceAmount = filteredTransactions.sumOf { it.amount }
        val incomeAmount = filteredTransactions.filter { it.amount > 0 }.sumOf { it.amount }
        val expenseAmount = filteredTransactions.filter { it.amount < 0 }.sumOf { -it.amount }

        binding.tvBalanceAmount.text = "Rp %.0f".format(balanceAmount)
        binding.tvIncomeAmount.text = "Rp %.0f".format(incomeAmount)
        binding.tvExpenseAmount.text = "Rp %.0f".format(expenseAmount)

        updateChart(filteredTransactions)
    }
}
