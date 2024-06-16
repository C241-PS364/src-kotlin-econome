package com.dicoding.econome.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.dicoding.econome.R
import com.dicoding.econome.databinding.ActivityRegisterBinding
import com.dicoding.econome.model.MainViewModel
import com.dicoding.econome.model.ViewModelFactory
import com.dicoding.econome.response.Result

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory(this)
    }
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        playAnimation()

        binding.login.setOnClickListener {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(
                i,
                ActivityOptionsCompat.makeSceneTransitionAnimation(this@RegisterActivity).toBundle()
            )
        }

        val gender = arrayOf("Male", "Female")
        val major = arrayOf("Psychology", "Economics", "Computer Science", "Engineering", "Biology")

        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, gender)
        binding.genderInput.setAdapter(genderAdapter)

        val majorAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, major)
        binding.majorInput.setAdapter(majorAdapter)

        binding.apply {

            btnRegist.setOnClickListener {
                val username = usernameInput.text.toString()
                val pass = passInput.text.toString()
                val name = textInput.text.toString()
                val gender = genderInput.text.toString()
                val major = majorInput.text.toString()
                val ageStr = ageInput.text.toString()
                when {
                    username.isEmpty() -> {
                        usernameInput.error = resources.getString(R.string.emptymail)
                    }
                    pass.isEmpty() -> {
                        passInput.error = resources.getString(R.string.emptypass)
                    }
                    name.isEmpty() -> {
                        textInput.error = resources.getString(R.string.emptyname)
                    }
                    gender.isEmpty() -> {
                        genderInput.error = resources.getString(R.string.emptygender)
                    }
                    major.isEmpty() -> {
                        majorInput.error = resources.getString(R.string.emptymajor)
                    }
                    ageStr.isEmpty() -> {
                        ageInput.error = resources.getString(R.string.emptyage)
                    }
                    else -> {
                        val age = ageStr.toIntOrNull()
                        if (age != null) {
                            register(username, pass, name, gender, major, age)
                        } else {
                            ageInput.error = resources.getString(R.string.invalidage)
                        }
                    }
                }
            }
        }
    }

    fun register(
        username: String,
        pass: String,
        name: String,
        gender: String,
        major: String,
        age: Int
    ) {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.loading)
        dialog.setCancelable(false)
        if (dialog.window != null) {
            dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        }
        val i = Intent(this, LoginActivity::class.java)

        mainViewModel.register(username, pass, name, gender, major, age)
        mainViewModel.registerResponse.observe(this) { result ->
            dialog.apply {
                when (result) {
                    is Result.Loading -> {
                        show()
                    }

                    is Result.Success -> {
                        cancel()
                        val registerResponse = result.data
                        if (!registerResponse.error) {
                            if (pass.length > 7) {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    resources.getString(R.string.regist),
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(i)
                            } else {
                                cancel()
                                Toast.makeText(
                                    this@RegisterActivity,
                                    resources.getString(R.string.errorpass),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            // If the error message indicates that the token has expired
                            if (registerResponse.message == "Auth token expired") {
                                // Get the saved refresh token from SharedPreferences
                                val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
                                val savedRefreshToken = sharedPreferences.getString("refreshToken", null)
                                // Call the refreshToken function
                                mainViewModel.refreshToken(savedRefreshToken ?: "")
                            } else {
                                cancel()
                                Toast.makeText(
                                    this@RegisterActivity,
                                    registerResponse.message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                    is Result.Error -> {
                        cancel()
                        Toast.makeText(this@RegisterActivity, result.error, Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
    }

    private fun playAnimation() {
        binding.apply {
            val title = ObjectAnimator.ofFloat(sign, View.ALPHA, 1f).setDuration(DURATION)
            val email = ObjectAnimator.ofFloat(email, View.ALPHA, 1f).setDuration(DURATION)
            val name = ObjectAnimator.ofFloat(name, View.ALPHA, 1f).setDuration(DURATION)
            val pass = ObjectAnimator.ofFloat(password, View.ALPHA, 1f).setDuration(DURATION)
            val button = ObjectAnimator.ofFloat(btnRegist, View.ALPHA, 1f).setDuration(DURATION)
            val sub1 = ObjectAnimator.ofFloat(login2, View.ALPHA, 1f).setDuration(DURATION)
            val sub2 = ObjectAnimator.ofFloat(login, View.ALPHA, 1f).setDuration(DURATION)

            val together = AnimatorSet().apply {
                playTogether(sub1, sub2)
            }

            AnimatorSet().apply {
                playSequentially(title, name, email, pass, button, together)
                start()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val DURATION: Long = 333
    }
}
