package com.example.explorandes

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    
    private val splashTimeOut: Long = 2000 // 2 seconds
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
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
                    // Check if MainActivity exists, otherwise use HomeActivity
                    val intent = if (isActivityAvailable(MainActivity::class.java.name)) {
                        Intent(this@SplashActivity, MainActivity::class.java)
                    } else {
                        Intent(this@SplashActivity, HomeActivity::class.java)
                    }
                    startActivity(intent)
                    finish()
                }, 500)
            }
        })
    }
    
    private fun isActivityAvailable(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}