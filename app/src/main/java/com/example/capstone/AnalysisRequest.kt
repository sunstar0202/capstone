package com.example.capstone

data class AnalysisRequest(
    val label: String,
    val score: Float,
    val deviceId: String = "tablet-01"
)