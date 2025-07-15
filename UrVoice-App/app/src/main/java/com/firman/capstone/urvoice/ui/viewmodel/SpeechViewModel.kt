package com.firman.capstone.urvoice.ui.viewmodel

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firman.capstone.urvoice.data.remote.models.SpeechResponse
import com.firman.capstone.urvoice.data.repository.speech.SpeechRepository
import com.firman.capstone.urvoice.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SpeechViewModel @Inject constructor(
    private val speechRepository: SpeechRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "SpeechViewModel"
    }

    private val _speechToTextState = MutableStateFlow<ResultState<SpeechResponse.Data>>(ResultState.Initial)
    val speechToTextState: StateFlow<ResultState<SpeechResponse.Data>> = _speechToTextState.asStateFlow()

    private val _audioFileName = MutableStateFlow("")
    val audioFileName: StateFlow<String> = _audioFileName.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _convertedText = MutableStateFlow("")
    val convertedText: StateFlow<String> = _convertedText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun startRecording() {
        try {
            Log.d(TAG, "Starting recording...")

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "AUDIO_$timestamp.3gp"
            audioFile = File(context.cacheDir, fileName)

            _audioFileName.value = fileName

            Log.d(TAG, "Audio file path: ${audioFile?.absolutePath}")

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                try {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setOutputFile(audioFile?.absolutePath)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                    Log.d(TAG, "Preparing MediaRecorder...")
                    prepare()

                    Log.d(TAG, "Starting MediaRecorder...")
                    start()

                    _isRecording.value = true
                    _speechToTextState.value = ResultState.Initial
                    _convertedText.value = ""
                    _errorMessage.value = null

                    Log.d(TAG, "Recording started successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error in MediaRecorder setup", e)
                    throw e
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            _errorMessage.value = "Gagal memulai rekaman: ${e.message}"
            _isRecording.value = false

            try {
                mediaRecorder?.release()
            } catch (releaseException: Exception) {
                Log.e(TAG, "Error releasing MediaRecorder", releaseException)
            }
            mediaRecorder = null
        }
    }

    fun stopRecordingAndTranscribe() {
        try {
            Log.d(TAG, "Stopping recording...")

            mediaRecorder?.apply {
                try {
                    stop()
                    Log.d(TAG, "MediaRecorder stopped")
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping MediaRecorder", e)
                }

                try {
                    release()
                    Log.d(TAG, "MediaRecorder released")
                } catch (e: Exception) {
                    Log.e(TAG, "Error releasing MediaRecorder", e)
                }
            }

            mediaRecorder = null
            _isRecording.value = false

            audioFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    Log.d(TAG, "Audio file exists, size: ${file.length()} bytes")
                    transcribeAudio(file)
                } else {
                    Log.e(TAG, "Audio file doesn't exist or is empty")
                    _errorMessage.value = "File audio tidak valid atau kosong"
                }
            } ?: run {
                Log.e(TAG, "Audio file is null")
                _errorMessage.value = "File audio tidak ditemukan"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            _errorMessage.value = "Gagal menghentikan rekaman: ${e.message}"
            _isRecording.value = false
        }
    }

    private fun transcribeAudio(audioFile: File) {
        viewModelScope.launch {
            _speechToTextState.value = ResultState.Loading
            try {
                Log.d(TAG, "Starting transcription for file: ${audioFile.absolutePath}")
                speechRepository.speechToText(audioFile).collect { result ->
                    _speechToTextState.value = result
                    when (result) {
                        is ResultState.Success -> {
                            _convertedText.value = result.data.text ?: ""
                            _errorMessage.value = null
                            // Update audioFileName from API response if available
                            result.data.audioFileName?.let { filename ->
                                _audioFileName.value = filename
                                Log.d(TAG, "Updated filename from API: $filename")
                            }
                            Log.d(TAG, "Transcription successful: ${result.data.text}")
                        }
                        is ResultState.Error -> {
                            _errorMessage.value = result.errorMessage
                            Log.e(TAG, "Transcription error: ${result.errorMessage}")
                        }
                        is ResultState.Loading -> {
                            Log.d(TAG, "Transcription in progress...")
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during transcription", e)
                _speechToTextState.value = ResultState.Error(e.message ?: "Terjadi kesalahan saat transkripsi.")
                _errorMessage.value = e.message ?: "Terjadi kesalahan saat transkripsi."
            }
        }
    }

    fun resetState() {
        Log.d(TAG, "Resetting state")
        _speechToTextState.value = ResultState.Initial
        _convertedText.value = ""
        _errorMessage.value = null
        _isRecording.value = false
        _audioFileName.value = ""

        // Clean up audio file
        audioFile?.let { file ->
            try {
                if (file.exists()) {
                    file.delete()
                    Log.d(TAG, "Audio file deleted")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting audio file", e)
            }
        }
        audioFile = null
    }

    fun retrySpeechToText() {
        audioFile?.let { transcribeAudio(it) }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared, cleaning up...")

        try {
            mediaRecorder?.apply {
                if (_isRecording.value) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up MediaRecorder in onCleared", e)
        }

        mediaRecorder = null

        // Clean up audio file
        audioFile?.let { file ->
            try {
                if (file.exists()) {
                    file.delete()
                    Log.d(TAG, "Audio file deleted in onCleared")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting audio file in onCleared", e)
            }
        }
    }
}