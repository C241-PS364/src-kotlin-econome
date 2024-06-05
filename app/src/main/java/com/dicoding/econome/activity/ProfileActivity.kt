package com.dicoding.econome.activity

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dicoding.econome.R
import com.dicoding.econome.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.selectedItemId = R.id.miProfile

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
    }
}