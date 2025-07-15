package com.firman.capstone.urvoice.data.repository.speech

import com.firman.capstone.urvoice.data.remote.models.SpeechResponse
import com.firman.capstone.urvoice.utils.ResultState
import kotlinx.coroutines.flow.Flow
import java.io.File

interface SpeechRepository {
    suspend fun speechToText(audioFile: File): Flow<ResultState<SpeechResponse.Data>>
}