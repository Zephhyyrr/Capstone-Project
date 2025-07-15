package com.firman.capstone.urvoice.data.repository.history

import com.firman.capstone.urvoice.data.local.datastore.AuthPreferences
import com.firman.capstone.urvoice.data.remote.models.HistoryResponse
import com.firman.capstone.urvoice.data.remote.request.HistoryRequest
import com.firman.capstone.urvoice.data.remote.service.HistoryService
import com.firman.capstone.urvoice.utils.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val historyService: HistoryService
) : HistoryRepository {

    override suspend fun saveHistory(
        audioFileName: String,
        originalParagraph: String,
        correctedParagraph: String,
        grammarAnalysis: List<HistoryRequest.GrammarAnalysis>
    ): Flow<ResultState<HistoryResponse.Data>> = flow {
        emit(ResultState.Loading)

        val token = authPreferences.authToken.first() ?: ""
        val request = HistoryRequest(
            audioFileName = audioFileName,
            originalParagraph = originalParagraph,
            correctedParagraph = correctedParagraph,
            grammarAnalysis = grammarAnalysis
        )

        val response = historyService.saveHistory("Bearer $token", request)
        val data = response.data // langsung, karena ini objek bukan List

        if (response.success && data != null) {
            emit(ResultState.Success(data))
        } else {
            emit(ResultState.Error(response.message ?: "Failed to save history"))
        }
    }.catch { e ->
        emit(ResultState.Error(e.message ?: "Unexpected error occurred"))
    }

    override suspend fun getAllHistory(): Flow<ResultState<List<HistoryResponse.Data>>> = flow {
        emit(ResultState.Loading)

        val token = authPreferences.authToken.first() ?: ""
        val response = historyService.getAllHistory("Bearer $token")

        if (response.success && response.data != null) {
            emit(ResultState.Success(response.data))
        } else {
            emit(ResultState.Error(response.message ?: "Failed to fetch history"))
        }
    }.catch { e ->
        emit(ResultState.Error(e.message ?: "Unexpected error occurred"))
    }

    override suspend fun getHistoryById(id: Int): Flow<ResultState<HistoryResponse.Data>> = flow {
        emit(ResultState.Loading)

        val token = authPreferences.authToken.first() ?: ""
        val response = historyService.getHistoryById("Bearer $token", id)
        val data = response.data

        if (response.success && data != null) {
            emit(ResultState.Success(data))
        } else {
            emit(ResultState.Error(response.message ?: "Failed to fetch history detail"))
        }
    }.catch { e ->
        emit(ResultState.Error(e.message ?: "Unexpected error occurred"))
    }
}
