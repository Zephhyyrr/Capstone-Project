package com.firman.capstone.urvoice.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firman.capstone.urvoice.data.remote.models.AnalyzeResponse
import com.firman.capstone.urvoice.data.repository.analyze.AnalyzeRepository
import com.firman.capstone.urvoice.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyzeViewModel @Inject constructor(
    private val analyzeRepository: AnalyzeRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AnalyzeViewModel"
    }

    private val _analyzeState = MutableStateFlow<ResultState<AnalyzeResponse>>(ResultState.Initial)
    val analyzeState: StateFlow<ResultState<AnalyzeResponse>> = _analyzeState.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _audioFileName = MutableStateFlow("")
    val audioFileName: StateFlow<String> = _audioFileName.asStateFlow()

    fun setInputData(text: String, fileName: String) {
        _inputText.value = text
        _audioFileName.value = fileName
        Log.d(TAG, "Input data set - Text: $text, FileName: $fileName")
    }

    fun analyzeText() {
        if (_inputText.value.isEmpty()) {
            _analyzeState.value = ResultState.Error("Text tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting text analysis...")
                analyzeRepository.analyzeText(_inputText.value, _audioFileName.value).collect { result ->
                    _analyzeState.value = result
                    when (result) {
                        is ResultState.Success -> {
                            Log.d(TAG, "Analysis successful")
                        }
                        is ResultState.Error -> {
                            Log.e(TAG, "Analysis error: ${result.errorMessage}")
                        }
                        is ResultState.Loading -> {
                            Log.d(TAG, "Analysis in progress...")
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during analysis", e)
                _analyzeState.value = ResultState.Error(e.message ?: "Terjadi kesalahan saat analisis.")
            }
        }
    }

    fun retryAnalysis() {
        analyzeText()
    }

    fun resetState() {
        _analyzeState.value = ResultState.Initial
        _inputText.value = ""
        _audioFileName.value = ""
    }
}