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
import com.dicoding.econome.databinding.ActivityEditProfileBinding
import com.dicoding.econome.model.MainViewModel
import com.dicoding.econome.model.ViewModelFactory
import com.dicoding.econome.response.Result

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory(this)
    }
    private lateinit var dialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        playAnimation()

        binding.btnCancel.setOnClickListener {
            val i = Intent(this, ProfileActivity::class.java)
            startActivity(
                i,
                ActivityOptionsCompat.makeSceneTransitionAnimation(this@EditProfileActivity)
                    .toBundle()
            )
        }

        val gender = arrayOf("Male", "Female")
        val major = arrayOf("Psychology", "Economics", "Computer Science", "Engineering", "Biology")

        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, gender)
        binding.genderInput.setAdapter(genderAdapter)

        val majorAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, major)
        binding.majorInput.setAdapter(majorAdapter)

        binding.apply {
            btnSave.setOnClickListener {
                val username = usernameInput.text.toString()
                val name = textInput.text.toString()
                val gender = genderInput.text.toString()
                val major = majorInput.text.toString()
                val ageStr = ageInput.text.toString()
                when {
                    username.isEmpty() -> {
                        usernameInput.error = resources.getString(R.string.emptymail)
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
                            updateProfile(
                                this@EditProfileActivity,
                                username,
                                name,
                                gender,
                                major,
                                age
                            )
                        } else {
                            ageInput.error = resources.getString(R.string.invalidage)
                        }
                    }
                }
            }
        }
    }

    fun updateProfile(
        context: Context,
        username: String,
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
        val i = Intent(this, ProfileActivity::class.java)

        mainViewModel.update(context, username, name, gender, major, age)
        mainViewModel.updateResponse.observe(this) { result ->
            dialog.apply {
                when (result) {
                    is Result.Loading -> {
                        show()
                    }

                    is Result.Success -> {
                        cancel()
                        val updateProfileResponse = result.data
                        if (!updateProfileResponse.error) {
                            if (updateProfileResponse.message == "Auth token expired") {
                                // Get the saved refresh token from SharedPreferences
                                val sharedPreferences =
                                    getSharedPreferences("UserData", Context.MODE_PRIVATE)
                                val savedRefreshToken =
                                    sharedPreferences.getString("refreshToken", null)
                                // Call the refreshToken function
                                mainViewModel.refreshToken(savedRefreshToken ?: "")
                            } else {
                                cancel()
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    updateProfileResponse.message,
                                    Toast.LENGTH_LONG
                                ).show()
                                val intent =
                                    Intent(this@EditProfileActivity, ProfileActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }

                    is Result.Error -> {
                        cancel()
                        Toast.makeText(this@EditProfileActivity, result.error, Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
    }

    private fun playAnimation() {
        binding.apply {
            val email = ObjectAnimator.ofFloat(email, View.ALPHA, 1f).setDuration(DURATION)
            val name = ObjectAnimator.ofFloat(name, View.ALPHA, 1f).setDuration(DURATION)
            val button = ObjectAnimator.ofFloat(btnSave, View.ALPHA, 1f).setDuration(DURATION)
            val button2 = ObjectAnimator.ofFloat(btnCancel, View.ALPHA, 1f).setDuration(DURATION)


            AnimatorSet().apply {
                playSequentially(email, name, email, button, button2)
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

