package com.firman.capstone.urvoice.data.repository.login

import android.util.Log
import com.firman.capstone.urvoice.data.local.datastore.AuthPreferences
import com.firman.capstone.urvoice.data.remote.request.LoginRequest
import com.firman.capstone.urvoice.data.remote.models.LoginResponse
import com.firman.capstone.urvoice.data.remote.service.LoginService
import com.firman.capstone.urvoice.utils.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val loginService: LoginService,
    private val authPreferences: AuthPreferences
) : LoginRepository {

    override suspend fun login(email: String, password: String): ResultState<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val currentToken = authPreferences.authToken.firstOrNull()
                Log.d("AuthDebug", "Token saat ini sebelum login: $currentToken")

                val request = LoginRequest(email = email, password = password)
                val response = loginService.login(request)

                if (response.success) {
                    response.data?.refreshToken?.let { refreshToken ->
                        authPreferences.saveAuthToken(refreshToken)
                        Log.d("AuthDebug", "Token baru setelah login: $refreshToken")
                    }
                    ResultState.Success(response, response.message)
                } else {
                    ResultState.Error(response.message ?: "Login failed")
                }

            } catch (e: Exception) {
                Log.e("LoginError", "Exception during login", e)
                ResultState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    override suspend fun refreshSession(email: String, password: String): ResultState<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val currentToken = authPreferences.authToken.firstOrNull()
                Log.d("AuthDebug", "Token saat ini sebelum refresh: $currentToken")

                val request = LoginRequest(email = email, password = password)
                val response = loginService.login(request)

                if (response.success) {
                    response.data?.refreshToken?.let { refreshToken ->
                        authPreferences.saveAuthToken(refreshToken)
                        Log.d("AuthDebug", "Token baru setelah refresh: $refreshToken")
                    }
                    ResultState.Success(Unit, "Session refreshed")
                } else {
                    ResultState.Error(response.message ?: "Refresh session failed")
                }

            } catch (e: Exception) {
                Log.e("RefreshError", "Exception during session refresh", e)
                ResultState.Error(e.message ?: "Unknown error during refresh")
            }
        }
    }

    override suspend fun isUserLoggedIn(): Boolean {
        return try {
            val token = authPreferences.authToken.firstOrNull()
            !token.isNullOrEmpty()
        } catch (e: Exception) {
            Log.e("LoginRepository", "Error checking login status", e)
            false
        }
    }

    override suspend fun logout(): ResultState<Unit> {
        return try {
            authPreferences.clearSession()
            ResultState.Success(Unit, "Logout successful")
        } catch (e: Exception) {
            Log.e("LoginRepository", "Error during logout", e)
            ResultState.Error(e.message ?: "Logout failed")
        }
    }
}