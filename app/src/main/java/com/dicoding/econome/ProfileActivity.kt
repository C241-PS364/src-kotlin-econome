package com.dicoding.econome

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.econome.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.selectedItemId = R.id.miPerson

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.miHome -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.miWallet -> {
                    startActivity(Intent(this, TransactionActivity::class.java))
                    true
                }
                R.id.miReport -> {
                    startActivity(Intent(this, ReportActivity::class.java))
                    true
                }
                R.id.miPerson -> {
                    true
                }
                else -> false
            }
        }

        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(2).isEnabled = false

    }
}