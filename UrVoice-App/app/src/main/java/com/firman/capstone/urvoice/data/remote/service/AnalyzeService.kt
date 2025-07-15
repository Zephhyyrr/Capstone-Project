package com.firman.capstone.urvoice.data.remote.service

import com.firman.capstone.urvoice.data.remote.models.AnalyzeResponse
import com.firman.capstone.urvoice.data.remote.request.AnalyzeRequest
import com.firman.capstone.urvoice.utils.ApiConstant
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AnalyzeService {
    @POST(ApiConstant.ANALYZE_TEXT)
    suspend fun analyzeText(
        @Header("Authorization") authorization: String,
        @Body request: AnalyzeRequest
    ): AnalyzeResponse
}