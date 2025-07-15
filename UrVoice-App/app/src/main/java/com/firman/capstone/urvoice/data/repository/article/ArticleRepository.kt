package com.firman.capstone.urvoice.data.repository.article

import com.firman.capstone.urvoice.data.remote.models.ArticleResponse
import com.firman.capstone.urvoice.utils.ResultState
import kotlinx.coroutines.flow.Flow

interface ArticleRepository {
    suspend fun getAllArticle(): Flow<ResultState<List<ArticleResponse.Data>>>
    suspend fun getArticleById(id: Int): Flow<ResultState<ArticleResponse.Data>>
}