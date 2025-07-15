package com.firman.capstone.urvoice.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firman.capstone.urvoice.data.remote.models.ArticleResponse
import com.firman.capstone.urvoice.data.repository.article.ArticleRepository
import com.firman.capstone.urvoice.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _articles =
        MutableStateFlow<ResultState<List<ArticleResponse.Data>>>(ResultState.Initial)
    val articles: StateFlow<ResultState<List<ArticleResponse.Data>>> = _articles
    private val _articleDetail = MutableStateFlow<ResultState<ArticleResponse.Data>>(ResultState.Initial)
    val articleDetail: StateFlow<ResultState<ArticleResponse.Data>> = _articleDetail

    init {
        getAllArticles()
    }

    fun getAllArticles() {
        viewModelScope.launch {
            articleRepository.getAllArticle().collect { result ->
                _articles.value = when (result) {
                    is ResultState.Success -> ResultState.Success(
                        data = result.data,
                        successMessage = result.successMessage
                    )
                    is ResultState.Error -> ResultState.Error(result.errorMessage)
                    is ResultState.Loading -> ResultState.Loading
                    is ResultState.Initial -> ResultState.Initial
                }
            }
        }
    }

    fun getArticleById(id: Int) {
        viewModelScope.launch {
            articleRepository.getArticleById(id).collect { result ->
                when (result) {
                    is ResultState.Success -> {
                        Log.d("ArticleViewModel", "Success getArticleById: ${result.data}")
                        _articleDetail.value = ResultState.Success(
                            data = result.data,
                            successMessage = result.successMessage
                        )
                    }
                    is ResultState.Error -> {
                        Log.e("ArticleViewModel", "Error: ${result.errorMessage}")
                        _articleDetail.value = ResultState.Error(result.errorMessage)
                    }
                    is ResultState.Loading -> {
                        _articleDetail.value = ResultState.Loading
                    }
                    is ResultState.Initial -> {
                        _articleDetail.value = ResultState.Initial
                    }
                }
            }
        }
    }
}
