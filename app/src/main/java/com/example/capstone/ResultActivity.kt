package com.example.capstone

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)


        val lastLabel = intent.getStringExtra("LAST_LABEL") ?: "PET"
        val lastScore = intent.getFloatExtra("LAST_SCORE", 0.0f)

        val btnBack = findViewById<Button>(R.id.btnBackToStart)
        btnBack?.setOnClickListener {
            finish()
        }

        val btnReviewReport = findViewById<Button>(R.id.btnReview)
        btnReviewReport?.setOnClickListener {
            showReportDialogAgain(lastLabel, lastScore)
        }
    }


    private fun showReportDialogAgain(label: String, score: Float) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_detail)
        dialog.setCancelable(true)

        val btnConfirm = dialog.findViewById<Button>(R.id.btnClose)
        val dialogScore = dialog.findViewById<TextView>(R.id.DialogScore)
        dialogScore?.text = "오염도 점수: ${score.toInt()}%"

        val title = dialog.findViewById<TextView>(R.id.title)
        val detail1 = dialog.findViewById<TextView>(R.id.detail1)
        val detail2 = dialog.findViewById<TextView>(R.id.detail2)
        val detail3 = dialog.findViewById<TextView>(R.id.detail3)
        val status1 = dialog.findViewById<TextView>(R.id.Status1)
        val status2 = dialog.findViewById<TextView>(R.id.Status2)
        val status3 = dialog.findViewById<TextView>(R.id.Status3)

        // [오염도 스코어에 따른 3단계 분기 세팅] - MainActivity와 완벽 일치
        if (score <= 5.0f) {
            status1?.text = "완료 (통과)"
            status1?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            status2?.text = "깨끗함"
            status2?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            status3?.text = "양호"
            status3?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        } else if (score <= 15.0f) {
            status1?.text = "미흡 (주의)"
            status1?.setTextColor(android.graphics.Color.parseColor("#FF9800"))
            status2?.text = "깨끗함"
            status2?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            status3?.text = "양호"
            status3?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        } else {
            status1?.text = "미흡 (감점)"
            status1?.setTextColor(android.graphics.Color.parseColor("#FF3B30"))
            status2?.text = "오염 검출"
            status2?.setTextColor(android.graphics.Color.parseColor("#FF3B30"))
            status3?.text = "불량"
            status3?.setTextColor(android.graphics.Color.parseColor("#FF3B30"))
        }


        when (label) {
            "CAN" -> {
                title?.text = "📊 CAN 정밀 분석 리포트"
                detail1?.text = "🔩 이물질 투입 여부"
                detail2?.text = "🥫 압착/찌그러짐 상태"
                detail3?.text = "🛡️ 캔 내부 세척도"
            }
            "GLASS" -> {
                title?.text = "📊 GLASS 정밀 분석 리포트"
                detail1?.text = "🍶 파손/크랙 여부"
                detail2?.text = "🪙 병뚜껑 분리 여부"
                detail3?.text = "🧼 유리 변색 상태"
            }
            else -> {
                title?.text = "📊 PET 정밀 분석 리포트"
                detail1?.text = "🏷️ 비닐 라벨 제거"
                detail2?.text = "💧 내부 잔여물/액체"
                detail3?.text = "🔍 재질 투명도"
            }
        }

        btnConfirm?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}