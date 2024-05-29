package com.dicoding.econome.activity

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dicoding.econome.R
import com.dicoding.econome.database.AppDatabase
import com.dicoding.econome.database.entity.Transaction
import com.dicoding.econome.databinding.ActivityReportBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {
            // Ambil data transaksi dari database
            val transactions = getTransactionsFromDatabase()

            // Kelompokkan transaksi berdasarkan tanggal dan kategori, dan hitung total pengeluaran per kategori per hari
            val expensePerDay = transactions.groupBy { it.date }.mapValues { (_, transactions) ->
                transactions.groupBy { it.category }.mapValues { (_, trans) -> trans.sumOf { it.amount } }
            }

            // Buat daftar BarEntry dan daftar label untuk sumbu X
            val entries = mutableListOf<BarEntry>()
            val labels = mutableListOf<String>()
            var index = 0f

            expensePerDay.forEach { (date, categoryMap) ->
                labels.add(date)
                categoryMap.forEach { (category, amount) ->
                    entries.add(BarEntry(index, amount.toFloat(), category))
                }
                index++
            }

            // Buat BarDataSet dengan daftar BarEntry
            val barDataSet = BarDataSet(entries, "Expenses")
            barDataSet.colors = listOf(
                ContextCompat.getColor(this@ReportActivity, R.color.colorEntertainment),
                ContextCompat.getColor(this@ReportActivity, R.color.colorFood),
                ContextCompat.getColor(this@ReportActivity, R.color.colorHealth),
                ContextCompat.getColor(this@ReportActivity, R.color.colorHousing),
                ContextCompat.getColor(this@ReportActivity, R.color.colorOther),
                ContextCompat.getColor(this@ReportActivity, R.color.colorTransportation)
            )

            // Set labels untuk kategori pada dataset
            barDataSet.setStackLabels(arrayOf("Entertainment", "Food", "Health and Beauty", "Housing", "Other", "Transportation"))

            // Buat BarData
            val barData = BarData(barDataSet)
            barData.setValueTextColor(ContextCompat.getColor(this@ReportActivity, R.color.black))
            barData.setValueTextSize(10f)

            // Tampilkan data di BarChart
            withContext(Dispatchers.Main) {
                binding.barChart.data = barData

                // Mengatur sumbu X
                val xAxis = binding.barChart.xAxis
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.setDrawGridLines(false)

                // Mengatur sumbu Y
                val leftAxis = binding.barChart.axisLeft
                leftAxis.setDrawGridLines(false)
                leftAxis.isInverted = true
                binding.barChart.axisRight.isEnabled = false

                // Mengatur legend
                val legend = binding.barChart.legend
                legend.isEnabled = true

                // Refresh chart
                binding.barChart.invalidate()
            }
        }

        binding.bottomNavigationView.selectedItemId = R.id.miReport

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val intent = when (item.itemId) {
                R.id.miHome -> Intent(this, MainActivity::class.java)
                R.id.miWallet -> Intent(this, TransactionActivity::class.java)
                R.id.miReport -> Intent(this, ReportActivity::class.java)
                R.id.miPerson -> Intent(this, ProfileActivity::class.java)
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

        binding.addTransactionFAB.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }

    // Fungsi untuk mengambil data transaksi dari database
    private suspend fun getTransactionsFromDatabase(): List<Transaction> {
        return withContext(Dispatchers.IO) {
            val dao = AppDatabase.getDatabase(this@ReportActivity).transactionDao()
            dao.getAll()
        }
    }
}
