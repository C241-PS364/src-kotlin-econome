package com.dicoding.econome

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.econome.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var transactions: ArrayList<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(2).isEnabled = false

        transactions = arrayListOf(
            Transaction("Gaji Bulanan", 10000000.00),
            Transaction("Bayar Kost", -2000000.00),
            Transaction("Beli Makanan", -50000.00),
            Transaction("Beli Baju", -100000.00),
            Transaction("Bayar Listrik", -200000.00),
            Transaction("Bayar Air", -100000.00),
            Transaction("Bayar Internet", -200000.00),
            Transaction("Bayar TV Kabel", -100000.00),
            Transaction("Bayar Asuransi", -500000.00),
            Transaction("Bayar Pajak", -1000000.00),
        )
        transactionAdapter = TransactionAdapter(transactions)
        linearLayoutManager = LinearLayoutManager(this)

        binding.rvTransactions.apply {
            adapter = transactionAdapter
            layoutManager = this@MainActivity.linearLayoutManager
        }

        updateDashboard()
    }

    private fun updateDashboard(){
        val balanceAmount = transactions.sumOf { it.amount }
        val incomeAmount = transactions.filter { it.amount > 0 }.sumOf { it.amount }
        val expenseAmount = balanceAmount - incomeAmount

        binding.tvBalanceAmount.text = "Rp %.2f".format(balanceAmount)
        binding.tvIncomeAmount.text = "Rp %.2f".format(incomeAmount)
        binding.tvExpenseAmount.text = "Rp %.2f".format(expenseAmount)
    }
}