package com.example.bookfinderapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class AboutUs : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_us) // Link to about_us.xml

        // Find the back icon
        val backIcon: ImageView = findViewById(R.id.backIcon)

        // Set click listener for back icon
        backIcon.setOnClickListener {
            // Navigate back to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close the AboutUs activity
        }
    }
}
