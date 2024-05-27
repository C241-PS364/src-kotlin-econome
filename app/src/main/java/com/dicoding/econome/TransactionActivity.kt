package com.dicoding.econome

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.econome.databinding.ActivityTransactionBinding

class TransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTransactionBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.bottomNavigationView.selectedItemId = R.id.miWallet

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.miHome -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }

                R.id.miWallet -> {
                    true
                }

                R.id.miReport -> {
                    startActivity(Intent(this, ReportActivity::class.java))
                    true
                }

                R.id.miPerson -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                else -> false
            }
        }
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(2).isEnabled = false
    }
}