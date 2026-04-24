package com.example.capstone

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/analysis-result")
    fun sendResult(
        @Body request: AnalysisRequest
    ): Call<AnalysisRequest>
}