package com.dicoding.econome.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.dicoding.econome.response.Result
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.dicoding.econome.R
import com.dicoding.econome.databinding.ActivityRegisterBinding
import com.dicoding.econome.model.MainViewModel
import com.dicoding.econome.model.ViewModelFactory

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

        binding.apply {
            btnRegist.setOnClickListener {
                val nama = textInput.text.toString()
                val email = emailInput.text.toString()
                val pass = passInput.text.toString()
                val age = ageInput.toString()
                val job = jobInput.toString()
                val gender = genderInput.toString()
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
                        register(nama, email, pass, age, job, gender)
                    }
                }
            }
        }
    }

    private fun register(nama: String, email: String, pass: String, age: String, job: String, gender: String) {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.loading)
        dialog.setCancelable(false)
        if (dialog.window != null) {
            dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        }
        val i = Intent(this, LoginActivity::class.java)

        mainViewModel.register(nama, email, pass, age, job, gender).observe(this) { result ->
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
            val image = ObjectAnimator.ofFloat(imageView, View.ALPHA, 1f).setDuration(DURATION)
            val subtitle = ObjectAnimator.ofFloat(welcome, View.ALPHA, 1f).setDuration(DURATION)
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
                playSequentially(title, image, subtitle, name, email, pass, button, together)
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