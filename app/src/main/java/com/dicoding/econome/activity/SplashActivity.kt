package com.dicoding.econome.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.dicoding.econome.R
import com.dicoding.econome.databinding.ActivitySplashBinding
import com.dicoding.econome.model.SettingFactory
import com.dicoding.econome.model.SettingViewModel
import com.dicoding.econome.util.SettingPreference

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private var isLogin = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = SettingPreference.getInstance(dataStore)
        val mainViewModel =
            ViewModelProvider(this, SettingFactory(pref))[SettingViewModel::class.java]
        mainViewModel.getThemeSettings().observe(this) {
            isLogin = it.token.isEmpty()
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isLogin) {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this@SplashActivity, WelcomeActivity::class.java)
                startActivity(intent)
            }
            finish()
        }, DURATION)

        Glide.with(this)
            .asGif()
            .load(R.drawable.sec_logo)
            .apply(RequestOptions.placeholderOf(R.drawable.sec_logo))
            .apply(RequestOptions.circleCropTransform())
            .into(binding.logo)

        // Load the fade-in animation
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        // Start the animation on the logo
        binding.logo.startAnimation(fadeInAnimation)
    }

    companion object {
        const val DURATION: Long = 3500
    }
}