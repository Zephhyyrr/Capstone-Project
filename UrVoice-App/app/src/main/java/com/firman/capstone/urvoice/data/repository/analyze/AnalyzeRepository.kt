package com.firman.capstone.urvoice.data.repository.analyze

import com.firman.capstone.urvoice.data.remote.models.AnalyzeResponse
import com.firman.capstone.urvoice.utils.ResultState
import kotlinx.coroutines.flow.Flow

interface AnalyzeRepository {
    suspend fun analyzeText(text: String, audioFileName: String): Flow<ResultState<AnalyzeResponse>>
}
