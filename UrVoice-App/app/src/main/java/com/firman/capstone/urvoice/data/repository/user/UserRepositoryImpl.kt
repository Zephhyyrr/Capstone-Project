package com.firman.capstone.urvoice.data.repository.user

import android.util.Log
import com.firman.capstone.urvoice.data.local.datastore.AuthPreferences
import com.firman.capstone.urvoice.data.remote.models.CurrentUserResponse
import com.firman.capstone.urvoice.data.remote.models.DeleteUserResponse
import com.firman.capstone.urvoice.data.remote.models.UserLogoutResponse
import com.firman.capstone.urvoice.data.remote.models.UserProfileResponse
import com.firman.capstone.urvoice.data.remote.models.UserResponse
import com.firman.capstone.urvoice.data.remote.request.UpdateUserRequest
import com.firman.capstone.urvoice.data.remote.service.UserService
import com.firman.capstone.urvoice.utils.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val userService: UserService
) : UserRepository {

    override suspend fun getCurrentUser(): Flow<ResultState<CurrentUserResponse>> = flow {
        emit(ResultState.Loading)

        val token = authPreferences.authToken.first()
        if (token.isNullOrEmpty()) {
            emit(ResultState.Error("Authentication token not found"))
            return@flow
        }

        val response = userService.getCurrentUser("Bearer $token")

        if (response.success) {
            emit(ResultState.Success(response))
        } else {
            emit(ResultState.Error(response.message ?: "Failed to fetch user"))
        }
    }.catch { e ->
        emit(ResultState.Error(handleException(e)))
    }

    override suspend fun updateUser(name: String, password: String): Flow<ResultState<UserResponse>> = flow {
        emit(ResultState.Loading)

        val token = authPreferences.authToken.first()
        if (token.isNullOrEmpty()) {
            emit(ResultState.Error("Authentication token not found"))
            return@flow
        }

        val request = UpdateUserRequest(name, password)
        val response = userService.updateUser("Bearer $token", request)

        if (response.success == true) {
            emit(ResultState.Success(response))
        } else {
            emit(ResultState.Error(response.message ?: "Failed to update user"))
        }
    }.catch { e ->
        emit(ResultState.Error(handleException(e)))
    }

    override suspend fun updateUserProfile(file: MultipartBody.Part): Flow<ResultState<UserProfileResponse>> = flow {
        emit(ResultState.Loading)

        val token = authPreferences.authToken.first()
        if (token.isNullOrEmpty()) {
            emit(ResultState.Error("Authentication token not found"))
            return@flow
        }

        val response = userService.getUserProfile("Bearer $token", file)

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.status == true) {
                emit(ResultState.Success(body))
            } else {
                emit(ResultState.Error(body?.message ?: "Upload failed"))
            }
        } else {
            emit(ResultState.Error("Upload failed: ${response.code()}"))
        }
    }.catch { e ->
        emit(ResultState.Error(handleException(e)))
    }

    override suspend fun logout(): Flow<ResultState<UserLogoutResponse>> = flow {
        emit(ResultState.Loading)

        val token = authPreferences.authToken.first()
        if (token.isNullOrEmpty()) {
            emit(ResultState.Error("Authentication token not found"))
            return@flow
        }

        val response = userService.logout("Bearer $token")

        if (response.success == true) {
            emit(ResultState.Success(response))
        } else {
            emit(ResultState.Error(response.message ?: "Failed to logout"))
        }
    }.catch { e ->
        emit(ResultState.Error(handleException(e)))
    }

    override suspend fun deleteUser(): Flow<ResultState<DeleteUserResponse>> = flow {
        emit(ResultState.Loading)

        val token = authPreferences.authToken.first()
        if (token.isNullOrEmpty()) {
            emit(ResultState.Error("Authentication token not found"))
            return@flow
        }

        val response = userService.deleteUser("Bearer $token")

        if (response.success == true) {
            emit(ResultState.Success(response))
        } else {
            emit(ResultState.Error(response.message ?: "Failed to delete user"))
        }
    }.catch { e ->
        emit(ResultState.Error(handleException(e)))
    }

    private fun handleException(e: Throwable): String {
        return when (e) {
            is HttpException -> {
                when (e.code()) {
                    401 -> "Unauthorized. Please login again."
                    403 -> "Access forbidden."
                    404 -> "Resource not found."
                    500 -> "Internal server error."
                    else -> "HTTP error: ${e.code()}"
                }
            }
            is IOException -> "Network error. Please check your connection."
            else -> e.message ?: "An unexpected error occurred"
        }
    }
}