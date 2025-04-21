package com.example.explorandes

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.explorandes.utils.SessionManager

class SplashActivity : AppCompatActivity() {

    private val splashTimeOut: Long = 2000 // 2 seconds
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        val appNameTextView = findViewById<TextView>(R.id.app_name_text)
        val fadeIn = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 1500
            fillAfter = true
        }

        appNameTextView.startAnimation(fadeIn)

        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                Handler(Looper.getMainLooper()).postDelayed({
                    // Check if user is logged in
                    if (sessionManager.isLoggedIn()) {
                        Log.d("SplashActivity", "User is logged in, going to HomeActivity")
                        startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                    } else {
                        Log.d("SplashActivity", "User is not logged in, going to MainActivity")
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    }
                    finish()
                }, 500)
            }
        })
    }
}