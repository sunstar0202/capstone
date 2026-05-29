package com.example.capstone

import android.app.Dialog
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


        val btnReviewReport = findViewById<Button>(R.id.btnReview)
        btnReviewReport?.setOnClickListener {
            showReportDialogAgain()
        }
    }


    private fun showReportDialogAgain() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_detail)
        dialog.setCancelable(true)

        val btnConfirm = dialog.findViewById<Button>(R.id.btnClose)
        btnConfirm?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}