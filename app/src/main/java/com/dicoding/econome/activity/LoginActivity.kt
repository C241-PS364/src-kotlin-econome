package com.dicoding.econome.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.dicoding.econome.R
import com.dicoding.econome.databinding.ActivityLoginBinding
import com.dicoding.econome.model.MainViewModel
import com.dicoding.econome.model.SettingFactory
import com.dicoding.econome.model.SettingViewModel
import com.dicoding.econome.model.ViewModelFactory
import com.dicoding.econome.response.Result
import com.dicoding.econome.util.SettingPreference
import com.dicoding.econome.util.SharedPrefManager
import com.github.mikephil.charting.BuildConfig

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory(this)
    }
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        playAnimation()

        if (BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        binding.signup.setOnClickListener {
            val i = Intent(this, RegisterActivity::class.java)
            startActivity(
                i,
                ActivityOptionsCompat.makeSceneTransitionAnimation(this@LoginActivity).toBundle()
            )
        }
        binding.apply {
            btnLogin.setOnClickListener {
                val username = usernameInput.text.toString()
                val pass = passInput.text.toString()
                when {
                    username.isEmpty() -> {
                        usernameInput.error = resources.getString(R.string.emptyusername)
                    }

                    pass.isEmpty() -> {
                        passInput.error = resources.getString(R.string.emptypass)
                    }

                    else -> {
                        login(username, pass)
                    }
                }
            }
        }
    }

    private fun login(username: String, pass: String) {
        val i = Intent(this, MainActivity::class.java)
        val pref = this.dataStore
        val data = SettingPreference.getInstance(pref)
        val viewModel = ViewModelProvider(this, SettingFactory(data))[SettingViewModel::class.java]

        dialog = Dialog(this)
        dialog.setContentView(R.layout.loading)
        dialog.setCancelable(false)
        if (dialog.window != null) {
            dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        }
        mainViewModel.login(username, pass)
        mainViewModel.loginResponse.observe(this) { result ->
            dialog.apply {
                when (result) {
                    is Result.Loading -> {
                        show()
                    }

                    is Result.Success -> {
                        cancel()
                        val loginResponse = result.data
                        if (loginResponse?.error == false) {
                            Toast.makeText(
                                this@LoginActivity,
                                resources.getString(R.string.login),
                                Toast.LENGTH_SHORT
                            ).show()
                            SharedPrefManager.setLoggedIn(
                                this@LoginActivity,
                                true
                            ) // Set login status to true
                            startActivity(i)
                        } else {
                            cancel()
                            Toast.makeText(
                                this@LoginActivity,
                                loginResponse?.message ?: resources.getString(R.string.errorpass),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    is Result.Error -> {
                        cancel()
                        Toast.makeText(this@LoginActivity, result.error, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun playAnimation() {
        binding.apply {
            val title = ObjectAnimator.ofFloat(sign, View.ALPHA, 1f).setDuration(DURATION)
            val username = ObjectAnimator.ofFloat(username, View.ALPHA, 1f).setDuration(DURATION)
            val pass = ObjectAnimator.ofFloat(password, View.ALPHA, 1f).setDuration(DURATION)
            val button = ObjectAnimator.ofFloat(btnLogin, View.ALPHA, 1f).setDuration(DURATION)
            val sub1 = ObjectAnimator.ofFloat(signup2, View.ALPHA, 1f).setDuration(DURATION)
            val sub2 = ObjectAnimator.ofFloat(signup, View.ALPHA, 1f).setDuration(DURATION)

            val together = AnimatorSet().apply {
                playTogether(sub1, sub2)
            }

            AnimatorSet().apply {
                playSequentially(title, username, pass, button, together)
                start()
            }
        }
    }

    companion object {
        const val DURATION: Long = 333
    }
}
