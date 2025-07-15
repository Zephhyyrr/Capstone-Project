package com.firman.capstone.urvoice.data.repository.article

import com.firman.capstone.urvoice.data.local.datastore.AuthPreferences
import com.firman.capstone.urvoice.data.remote.models.ArticleResponse
import com.firman.capstone.urvoice.data.remote.service.ArticleService
import com.firman.capstone.urvoice.utils.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ArticleRepositoryImpl @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val articleService: ArticleService
) : ArticleRepository {

    override suspend fun getAllArticle(): Flow<ResultState<List<ArticleResponse.Data>>> = flow {
        emit(ResultState.Loading)

        val token = authPreferences.authToken.first() ?: ""
        val response = articleService.getAllArticle("Bearer $token")

        if (response.success && response.data != null) {
            emit(ResultState.Success(response.data))
        } else {
            emit(ResultState.Error(response.message ?: "Failed to fetch articles"))
        }
    }.catch { e ->
        emit(ResultState.Error(e.message ?: "Unexpected error occurred"))
    }

    override suspend fun getArticleById(id: Int): Flow<ResultState<ArticleResponse.Data>> =
        flow {
            emit(ResultState.Loading)

            val token = authPreferences.authToken.first() ?: ""
            val response = articleService.getArticleById("Bearer $token", id)

            if (response.success && response.data != null) {
                emit(ResultState.Success(response.data))
            } else {
                emit(ResultState.Error(response.message ?: "Failed to fetch article"))
            }
        }.catch { e ->
            emit(ResultState.Error(e.message ?: "Unexpected error occurred"))
        }
}
