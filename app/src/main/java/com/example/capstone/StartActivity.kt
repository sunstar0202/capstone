package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start) // 아까 만든 홈 화면 레이아웃

        val btnStart = findViewById<Button>(R.id.btnStart)

        btnStart.setOnClickListener {
            // 버튼 클릭 시 MainActivity(카메라 화면)로 이동
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}