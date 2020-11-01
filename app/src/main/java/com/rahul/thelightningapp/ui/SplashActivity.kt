package com.rahul.thelightningapp.ui

import android.R.attr.button
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.rahul.thelightningapp.databinding.ActivitySplashBinding


class SplashActivity : AppCompatActivity() {
    private val splashTime: Long = 2000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val end: Float = binding.iv.translationX + 50
        ObjectAnimator.ofFloat(binding.iv, "translationY", end).apply {
            duration = 2000
            start()
        }
        Handler().postDelayed({
            val intent = Intent(this@SplashActivity, UserInputActivity::class.java)
            startActivity(intent)
            finish()
        }, splashTime)

    }
}