package com.example.capstone

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_result)


        val btnBack = findViewById<Button>(R.id.btnBackToStart)
        btnBack?.setOnClickListener {
            finish()
        }
    }
}