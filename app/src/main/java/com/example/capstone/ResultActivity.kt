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

        val displayScore = score.toInt()
        dialogScore?.text = "오염도 점수: ${displayScore}%"

        val title = dialog.findViewById<TextView>(R.id.title)
        val detail1 = dialog.findViewById<TextView>(R.id.detail1)
        val detail2 = dialog.findViewById<TextView>(R.id.detail2)
        val detail3 = dialog.findViewById<TextView>(R.id.detail3)
        val status1 = dialog.findViewById<TextView>(R.id.Status1)
        val status2 = dialog.findViewById<TextView>(R.id.Status2)
        val status3 = dialog.findViewById<TextView>(R.id.Status3)

        // 🌟 [수정 핵심] MainActivity와 100% 동일한 소수점 플래그 복원 알고리즘 적용
        val temp = ((score - displayScore) * 1000 + 0.5f).toInt()
        val c1 = (temp / 100) % 10
        val c2 = (temp / 10) % 10
        val c3 = temp % 10

        // 🌟 [수정 핵심] 소수점 자리에 숨겨진 개별 플래그 기반 텍스트 매핑 함수 도입
        fun bindStatusText(tv: TextView?, code: Int, type: Int) {
            if (type == 1) {
                when (code) {
                    1 -> {
                        tv?.text = "완료 (통과)"
                        tv?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                    }
                    2 -> {
                        tv?.text = "미흡 (주의)"
                        tv?.setTextColor(android.graphics.Color.parseColor("#FF9800"))
                    }
                    else -> {
                        tv?.text = "미흡 (감점)"
                        tv?.setTextColor(android.graphics.Color.parseColor("#FF3B30"))
                    }
                }
            } else if (type == 2) {
                if (code == 1) {
                    tv?.text = "깨끗함"
                    tv?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                } else {
                    tv?.text = "오염 검출"
                    tv?.setTextColor(android.graphics.Color.parseColor("#FF3B30"))
                }
            } else {
                if (code == 1) {
                    tv?.text = "양호"
                    tv?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                } else {
                    tv?.text = "불량"
                    tv?.setTextColor(android.graphics.Color.parseColor("#FF3B30"))
                }
            }
        }

        // 각 상태창에 정확하게 전달받은 플래그 값 바인딩
        bindStatusText(status1, c1, 1)
        bindStatusText(status2, c2, 2)
        bindStatusText(status3, c3, 3)

        when (label) {
            "CAN" -> {
                title?.text = "📊 CAN 정밀 분석 리포트"
                detail1?.text = "🔩 이물질 투입 여부"
                detail2?.text = "🥫 압착/찌러짐 상태"
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