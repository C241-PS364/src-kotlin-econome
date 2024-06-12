package com.dicoding.econome.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import com.dicoding.econome.R
import com.dicoding.econome.databinding.ActivityRegisterBinding
import com.dicoding.econome.model.MainViewModel
import com.dicoding.econome.model.ViewModelFactory
import com.dicoding.econome.response.Result
import java.util.Calendar

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

        // Check if the device version is greater than or equal to Lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Change the status bar color
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        }

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
        val gender = arrayOf(
            "Male",
            "Female",
        )
        val major = arrayOf(
            "Psychology",
            "Economics", "Computer Science",
            "Engineering",
            "Biology",
        )

        val genderadapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, gender)
        binding.genderInput.setAdapter(genderadapter)

        val majoradapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, major)
        binding.majorEditText.setAdapter(majoradapter)

        binding.apply {
            ageEditText.setOnClickListener {
                showDatePickerDialog(ageEditText)
            }

            btnRegist.setOnClickListener {
                val nama = textInput.text.toString()
                val email = emailInput.text.toString()
                val pass = passInput.text.toString()
                val age = ageEditText.text.toString()
                val major = majorEditText.text.toString()
                val gender = genderInput.text.toString()
                when {
                    nama.isEmpty() -> {
                        textInput.error = resources.getString(R.string.emptyname)
                    }

                    email.isEmpty() -> {
                        emailInput.error = resources.getString(R.string.emptymail)
                    }

                    pass.isEmpty() -> {
                        passInput.error = resources.getString(R.string.emptypass)
                    }

                    else -> {
                        register(nama, email, pass, age, major, gender)
                    }
                }
            }
        }
    }


    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                editText.setText(selectedDate)
            }, year, month, day)

        datePickerDialog.show()
    }

    private fun register(
        nama: String,
        email: String,
        pass: String,
        age: String,
        major: String,
        gender: String
    ) {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.loading)
        dialog.setCancelable(false)
        if (dialog.window != null) {
            dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        }
        val i = Intent(this, LoginActivity::class.java)

        mainViewModel.register(nama, email, pass, age, major, gender).observe(this) { result ->
            if (result != null) {
                dialog.apply {
                    when (result) {
                        is Result.Loading -> {
                            show()
                        }

                        is Result.Success -> {
                            cancel()
                            if (result.data?.error == false) {
                                if (binding.passInput.text?.length!! > 7) {
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
                                cancel()
                                Toast.makeText(
                                    this@RegisterActivity,
                                    result.data?.message.toString(),
                                    Toast.LENGTH_LONG
                                ).show()
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
    }

    private fun playAnimation() {
        binding.apply {
            val title = ObjectAnimator.ofFloat(sign, View.ALPHA, 1f).setDuration(DURATION)
//            val image = ObjectAnimator.ofFloat(imageView, View.ALPHA, 1f).setDuration(DURATION)
//            val subtitle = ObjectAnimator.ofFloat(welcome, View.ALPHA, 1f).setDuration(DURATION)
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