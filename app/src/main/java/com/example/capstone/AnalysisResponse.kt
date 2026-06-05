package com.example.capstone

data class AnalysisResponse(
    val id: Int,
    val label: String,
    val score: Float,
    val deviceId: String,
    val createdAt: String
)