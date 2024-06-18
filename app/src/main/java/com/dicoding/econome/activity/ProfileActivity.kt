package com.dicoding.econome.activity

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dicoding.econome.R
import com.dicoding.econome.auth.ApiConfig
import com.dicoding.econome.database.AppDatabase
import com.dicoding.econome.databinding.ActivityProfileBinding
import com.dicoding.econome.util.Repository
import com.dicoding.econome.util.SharedPrefManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the repository
        val authService = ApiConfig.api
        val userService = ApiConfig.userService
        val database = AppDatabase.getDatabase(this) // Replace with the actual method to get your AppDatabase instance

        repository = Repository(authService, userService, database)

        if (!SharedPrefManager.isLoggedIn(this)) {
            navigateToLogin()
        }

        repository.getProfile(this) { profileResponse, error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            } else {
                binding.email.text = profileResponse?.data?.username
                binding.name.text = profileResponse?.data?.name
                binding.gender.text = profileResponse?.data?.gender
                binding.major.text = profileResponse?.data?.major
                binding.age.text = profileResponse?.data?.age.toString()
            }
        }

        binding.editprofile.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, EditProfileActivity::class.java))
        }

        setupBottomNavigationView()
        setupLogoutButton()

        binding.addTransactionFAB.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("Login Status", "Is logged in: ${SharedPrefManager.isLoggedIn(this)}")

        if (!SharedPrefManager.isLoggedIn(this)) {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun setupBottomNavigationView() {
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
        binding.bottomNavigationView.itemIconTintList =
            ContextCompat.getColorStateList(this, R.color.bottom_nav_item_color)
        binding.bottomNavigationView.itemTextColor =
            ContextCompat.getColorStateList(this, R.color.bottom_nav_item_color)
    }

    private fun setupLogoutButton() {
        binding.logout.setOnClickListener {
            repository.logout(this) { error ->
                if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                } else {
                    SharedPrefManager.setLoggedIn(this, false) // Set login status to false
                    Toast.makeText(this, "Logout successful", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing on back press
    }
}
