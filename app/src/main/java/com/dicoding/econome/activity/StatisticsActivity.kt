package com.dicoding.econome.activity

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.econome.R
import com.dicoding.econome.adapter.TopSpendingAdapter
import com.dicoding.econome.database.AppDatabase
import com.dicoding.econome.database.entity.Transaction
import com.dicoding.econome.databinding.ActivityStatisticsBinding
import com.dicoding.econome.model.TopSpending
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class StatisticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatisticsBinding
    private val topSpendings = mutableListOf<TopSpending>()
    private lateinit var adapter: TopSpendingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
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

        // Setup bottom navigation
        setupBottomNavigation()

        // Initialize the TabLayout
        val tabLayoutTimeRange: TabLayout = findViewById(R.id.tabLayoutTimeRange)
        val timeRanges = resources.getStringArray(R.array.time_range)
        timeRanges.forEach { timeRange ->
            tabLayoutTimeRange.addTab(tabLayoutTimeRange.newTab().setText(timeRange))
        }

        tabLayoutTimeRange.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val timeRange = tab.text.toString()
                // Update your PieChart and TopSpending based on the selected time range
                fetchFiltered(timeRange)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Do nothing
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Do nothing
            }
        })

        // Fetch transactions and setup pie chart
        fetchFiltered("All Time")

        // Setup RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TopSpendingAdapter(
            topSpendings,
            tabLayoutTimeRange.getTabAt(tabLayoutTimeRange.selectedTabPosition)?.text.toString()
        )
        binding.recyclerView.adapter = adapter

        adapter.setOnItemClickListener { category ->
            val intent = Intent(this, TopSpendingDetailsActivity::class.java).apply {
                putExtra("CATEGORY", category)
                putExtra(
                    "TIME_RANGE",
                    tabLayoutTimeRange.getTabAt(tabLayoutTimeRange.selectedTabPosition)?.text.toString()
                )
            }
            startActivity(intent)
        }

    }

    private fun fetchFiltered(timeRange: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val allTransactions = getTransactionsFromDatabase()
            val currentDate = LocalDate.now()

            // Filter transactions based on the selected time range
            val filteredTransactions = when (timeRange) {
                "All Time" -> allTransactions
                "Last 7 Days" -> allTransactions.filter {
                    val transactionDate =
                        LocalDate.parse(it.date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    ChronoUnit.DAYS.between(transactionDate, currentDate) <= 7
                }

                "Last 30 Days" -> allTransactions.filter {
                    val transactionDate =
                        LocalDate.parse(it.date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    ChronoUnit.DAYS.between(transactionDate, currentDate) <= 30
                }

                else -> allTransactions
            }

            val expenseTransactions =
                filteredTransactions.filter { it.amount < 0 } // Filter only expenses
            val categorySums = expenseTransactions.groupBy { it.category }
                .mapValues { (_, trans) -> trans.sumOf { it.amount } }
            val categoryCounts = expenseTransactions.groupBy { it.category }
                .mapValues { (_, trans) -> trans.size }

            // Define the order of categories
            val categoriesOrder = listOf(
                "Food",
                "Health",
                "Transportation",
                "Housing",
                "Entertainment",
                "Other"
            )

            // Sort the entries according to the defined order
            val pieEntries = categoriesOrder.mapNotNull { category ->
                categorySums[category]?.let { PieEntry(abs(it.toFloat()), category) }
            }

            val pieDataSet = PieDataSet(pieEntries, "Expenses")

            pieDataSet.valueFormatter = CustomPercentFormatter(binding.pieChart)

            // Set colors for each category
            val categoryColors = mapOf(
                "Food" to R.color.colorFood,
                "Health" to R.color.colorHealth,
                "Transportation" to R.color.colorTransportation,
                "Housing" to R.color.colorHousing,
                "Entertainment" to R.color.colorEntertainment,
                "Other" to R.color.colorOther
            )
            pieDataSet.colors = categoriesOrder.map { category ->
                ContextCompat.getColor(
                    this@StatisticsActivity,
                    categoryColors[category] ?: R.color.colorOther
                )
            }
            pieDataSet.valueTextColor = Color.WHITE
            pieDataSet.valueTextSize = 12f

            val pieData = PieData(pieDataSet)

            withContext(Dispatchers.Main) {
                val categoryIndicatorContainer: FlexboxLayout = binding.categoryIndicatorContainer
                categoryIndicatorContainer.removeAllViews() // Clear old views
                categoriesOrder.forEachIndexed { index, category ->
                    val categoryIndicator = LayoutInflater.from(this@StatisticsActivity)
                        .inflate(R.layout.category_indicator, categoryIndicatorContainer, false)
                    // Set the color and text of the category indicator
                    val categoryColorView: View = categoryIndicator.findViewById(R.id.categoryColor)
                    val categoryTextView: TextView =
                        categoryIndicator.findViewById(R.id.categoryText)
                    (categoryColorView.background as GradientDrawable).setColor(
                        ContextCompat.getColor(
                            this@StatisticsActivity,
                            categoryColors[category] ?: R.color.colorOther
                        )
                    )
                    categoryTextView.text = category
                    categoryTextView.textSize= 10f

                    // Create new layout params for the category indicator
                    val layoutParams = FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        (16 * resources.displayMetrics.density).toInt() // Set the height to 16dp for example
                    )
                    // Set the margins (8dp for example)
                    val margin = (8 * resources.displayMetrics.density).toInt()
                    layoutParams.setMargins(margin, 4, margin, 4) // Reduced top and bottom margins
                    // Set the flex basis percent to 0.33 (which is 100% / 3) to limit 3 items per line
                    layoutParams.flexBasisPercent = if (index < 6) 0.25f else -1f
                    categoryIndicator.layoutParams = layoutParams

                    categoryIndicatorContainer.addView(categoryIndicator)
                }
                categoryIndicatorContainer.justifyContent = JustifyContent.CENTER


                binding.pieChart.data = pieData
                binding.pieChart.description.isEnabled = false
                binding.pieChart.isDrawHoleEnabled = true
                binding.pieChart.setHoleColor(Color.TRANSPARENT)
                binding.pieChart.setTransparentCircleColor(Color.TRANSPARENT)
                binding.pieChart.setDrawEntryLabels(false) // This line hides the labels
                binding.pieChart.setUsePercentValues(true)
                binding.pieChart.legend.isEnabled = false

                binding.pieChart.holeRadius = 50f

                val totalExpense = expenseTransactions.sumOf { abs(it.amount) }
                val formattedTotalExpense = if (totalExpense % 1 == 0.0) totalExpense.toInt() else totalExpense
                val centerText = "<b>Expense</b><br>Rp ${formattedTotalExpense}"
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    binding.pieChart.centerText = Html.fromHtml(centerText, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    @Suppress("DEPRECATION")
                    binding.pieChart.centerText = Html.fromHtml(centerText)
                }
                binding.pieChart.setCenterTextSize(12f)
                binding.pieChart.setCenterTextColor(Color.BLACK)

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

    class CustomPercentFormatter(private val pieChart: PieChart) : PercentFormatter(pieChart) {
        override fun getFormattedValue(value: Float): String {
            return if (value < 5) "" else super.getFormattedValue(value)
        }
    }

    private fun getCategoryIconRes(category: String): Int {
        return when (category) {
            "Food" -> R.drawable.ic_food
            "Other" -> R.drawable.ic_other
            "Health" -> R.drawable.ic_health
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
        binding.bottomNavigationView.itemIconTintList =
            ContextCompat.getColorStateList(this, R.color.bottom_nav_item_color)
        binding.bottomNavigationView.itemTextColor =
            ContextCompat.getColorStateList(this, R.color.bottom_nav_item_color)
    }

    private suspend fun getTransactionsFromDatabase(): List<Transaction> {
        return withContext(Dispatchers.IO) {
            val dao = AppDatabase.getDatabase(this@StatisticsActivity).transactionDao()
            dao.getAll()
        }
    }

    override fun onResume() {
        super.onResume()
        val tabLayoutTimeRange: TabLayout = findViewById(R.id.tabLayoutTimeRange)
        val selectedTimeRange = tabLayoutTimeRange.getTabAt(tabLayoutTimeRange.selectedTabPosition)?.text.toString()
        fetchFiltered(selectedTimeRange)
    }
}
