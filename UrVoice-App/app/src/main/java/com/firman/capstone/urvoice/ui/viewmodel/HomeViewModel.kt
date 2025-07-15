package com.firman.capstone.urvoice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firman.capstone.urvoice.data.remote.models.ArticleResponse
import com.firman.capstone.urvoice.data.remote.models.CurrentUserResponse
import com.firman.capstone.urvoice.data.remote.models.HistoryResponse
import com.firman.capstone.urvoice.data.repository.article.ArticleRepository
import com.firman.capstone.urvoice.data.repository.history.HistoryRepository
import com.firman.capstone.urvoice.data.repository.user.UserRepository
import com.firman.capstone.urvoice.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val articleRepository: ArticleRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<ResultState<CurrentUserResponse>>(ResultState.Initial)
    val userState: StateFlow<ResultState<CurrentUserResponse>> = _userState.asStateFlow()

    private val _articlesState =
        MutableStateFlow<ResultState<List<ArticleResponse.Data>>>(ResultState.Initial)
    val articlesState: StateFlow<ResultState<List<ArticleResponse.Data>>> =
        _articlesState.asStateFlow()

    private val _historyState =
        MutableStateFlow<ResultState<List<HistoryResponse.Data>>>(ResultState.Initial)
    val historyState: StateFlow<ResultState<List<HistoryResponse.Data>>> = _historyState.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        getCurrentUser()
        getAllArticles()
        getAllHistories()
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            _userState.value = ResultState.Loading
            try {
                userRepository.getCurrentUser().collect { result ->
                    _userState.value = result
                }
            } catch (e: Exception) {
                _userState.value = ResultState.Error("Failed to load user data: ${e.message}")
            }
        }
    }

    fun getAllArticles() {
        viewModelScope.launch {
            _articlesState.value = ResultState.Loading
            articleRepository.getAllArticle().collect { result ->
                _articlesState.value = result
            }
        }
    }

    fun getAllHistories() {
        viewModelScope.launch {
            _historyState.value = ResultState.Loading
            try {
                historyRepository.getAllHistory().collect { result ->
                    if (result is ResultState.Success) {
                        _historyState.value = ResultState.Success(result.data.toList())
                    } else {
                        _historyState.value = result
                    }
                }
            } catch (e: Exception) {
                _historyState.value = ResultState.Error("Failed to load history: ${e.message}")
            }
        }
    }

}