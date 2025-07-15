package com.firman.capstone.urvoice.data.repository.speech

import com.firman.capstone.urvoice.data.local.datastore.AuthPreferences
import com.firman.capstone.urvoice.data.remote.models.SpeechResponse
import com.firman.capstone.urvoice.data.remote.service.SpeechService
import com.firman.capstone.urvoice.utils.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class SpeechRepositoryImpl @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val speechService: SpeechService
) : SpeechRepository {

    override suspend fun speechToText(audioFile: File): Flow<ResultState<SpeechResponse.Data>> = flow {
        emit(ResultState.Loading)

        val token = authPreferences.authToken.first() ?: ""

        val requestFile = audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
        val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, requestFile)

        val response = speechService.speechToText("Bearer $token", audioPart)

        if (response.data != null) {
            emit(ResultState.Success(response.data))
        } else {
            emit(ResultState.Error("Speech data is null"))
        }
    }.catch { e ->
        emit(ResultState.Error(e.message ?: "Unexpected error occurred"))
    }
}