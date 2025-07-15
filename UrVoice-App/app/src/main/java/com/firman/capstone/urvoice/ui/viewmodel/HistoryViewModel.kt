package com.firman.capstone.urvoice.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firman.capstone.urvoice.data.remote.models.HistoryResponse
import com.firman.capstone.urvoice.data.remote.request.HistoryRequest
import com.firman.capstone.urvoice.data.repository.history.HistoryRepository
import com.firman.capstone.urvoice.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    companion object {
        private const val TAG = "HistoryViewModel"
    }

    private val _saveHistoryState =
        MutableStateFlow<ResultState<HistoryResponse.Data>>(ResultState.Initial)
    val saveHistoryState: StateFlow<ResultState<HistoryResponse.Data>> =
        _saveHistoryState.asStateFlow()

    private val _historyListState =
        MutableStateFlow<ResultState<List<HistoryResponse.Data>>>(ResultState.Initial)
    val historyListState: StateFlow<ResultState<List<HistoryResponse.Data>>> =
        _historyListState.asStateFlow()

    private val _historyDetailState =
        MutableStateFlow<ResultState<HistoryResponse.Data>>(ResultState.Initial)
    val historyDetailState: StateFlow<ResultState<HistoryResponse.Data>> =
        _historyDetailState.asStateFlow()

    fun saveHistory(
        audioFileName: String,
        originalParagraph: String,
        correctedParagraph: String,
        grammarAnalysis: List<HistoryRequest.GrammarAnalysis>
    ) {
        viewModelScope.launch {
            Log.d(TAG, "Starting saveHistory...")
            _saveHistoryState.value = ResultState.Loading

            historyRepository.saveHistory(
                audioFileName = audioFileName,
                originalParagraph = originalParagraph,
                correctedParagraph = correctedParagraph,
                grammarAnalysis = grammarAnalysis
            ).collect { result ->
                _saveHistoryState.value = result
                when (result) {
                    is ResultState.Success -> Log.d(
                        TAG,
                        "History saved successfully: ${result.data}"
                    )

                    is ResultState.Error -> Log.e(
                        TAG,
                        "Error saving history: ${result.errorMessage}"
                    )

                    is ResultState.Loading -> Log.d(TAG, "Saving history in progress...")
                    else -> Log.d(TAG, "Other state: $result")
                }
            }
        }
    }

    fun getAllHistory() {
        viewModelScope.launch {
            Log.d(TAG, "Fetching all history...")
            _historyListState.value = ResultState.Loading

            historyRepository.getAllHistory().collect { result ->
                _historyListState.value = result
                when (result) {
                    is ResultState.Success -> Log.d(
                        TAG,
                        "History list fetched, count: ${result.data.size}"
                    )

                    is ResultState.Error -> Log.e(
                        TAG,
                        "Error fetching history: ${result.errorMessage}"
                    )

                    is ResultState.Loading -> Log.d(TAG, "⏳ Loading history list...")
                    else -> {}
                }
            }
        }
    }

    fun getHistoryById(id: Int) {
        viewModelScope.launch {
            Log.d(TAG, "Fetching history detail for ID: $id")
            _historyDetailState.value = ResultState.Loading

            historyRepository.getHistoryById(id).collect { result ->
                _historyDetailState.value = result
                when (result) {
                    is ResultState.Success -> Log.d(TAG, "History detail fetched")
                    is ResultState.Error -> Log.e(
                        TAG,
                        "Error fetching history detail: ${result.errorMessage}"
                    )

                    is ResultState.Loading -> Log.d(TAG, "Loading history detail...")
                    else -> {}
                }
            }
        }
    }

    fun resetSaveState() {
        Log.d(TAG, "Resetting save state only")
        _saveHistoryState.value = ResultState.Initial
    }
}
