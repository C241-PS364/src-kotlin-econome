package com.dicoding.econome.activity

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.dicoding.econome.R
import com.dicoding.econome.adapter.TransactionAdapter
import com.dicoding.econome.database.AppDatabase
import com.dicoding.econome.database.entity.Transaction
import com.dicoding.econome.databinding.ActivityTransactionBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
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
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTransactionBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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
        binding.bottomNavigationView.itemIconTintList = ContextCompat.getColorStateList(this, R.color.bottom_nav_item_color)
        binding.bottomNavigationView.itemTextColor = ContextCompat.getColorStateList(this, R.color.bottom_nav_item_color)

        binding.addTransactionFAB.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
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
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTimeRange.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter
        }

        spinnerTimeRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val timeRange = parent.getItemAtPosition(position).toString()
                // Update your RecyclerView based on the selected time range
                fetchFiltered(timeRange, spinnerCategory.selectedItem.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val category = parent.getItemAtPosition(position).toString()
                // Update your RecyclerView based on the selected category
                fetchFiltered(spinnerTimeRange.selectedItem.toString(), category)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
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
        GlobalScope.launch {
            val allTransactions = db.transactionDao().getAll()

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
            val filteredTransactions = if (category == "All Categories") {
                filteredByTimeRange
            } else {
                filteredByTimeRange.filter { it.category == category }
            }

            transactions = filteredTransactions

            runOnUiThread {
                transactionAdapter.setData(transactions)
            }
        }
    }

    private fun undoDelete(){
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

            transactions = transactions.filter { it.id != transaction.id }
            runOnUiThread {
                transactionAdapter.setData(transactions)
                showSnackbar()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}