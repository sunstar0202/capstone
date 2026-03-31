package com.example.capstone

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var classifier: TrashClassifier
    private lateinit var bluetooth: BluetoothManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        classifier = TrashClassifier(this)
        bluetooth = BluetoothManager(" ") // 아두이노 맥주소

        // 블루투스 연결은 별도 스레드에서 실행 권장 (여기는 기본 틀)
        Thread {
            if (bluetooth.connect()) {
                runOnUiThread { Toast.makeText(this, "아두이노 연결 성공!", Toast.LENGTH_SHORT).show() }
            }
        }.start()

        // 분석 실행 예시 (나중에 버튼 이벤트에 연결)
        // val result = classifier.analyze(someBitmap)
        // if(result.second <= 15.0f) bluetooth.send("O")
    }
}