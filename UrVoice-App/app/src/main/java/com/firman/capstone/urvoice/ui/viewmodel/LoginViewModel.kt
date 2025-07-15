package com.firman.capstone.urvoice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firman.capstone.urvoice.data.local.datastore.AuthPreferences
import com.firman.capstone.urvoice.data.remote.models.LoginResponse
import com.firman.capstone.urvoice.data.repository.login.LoginRepository
import com.firman.capstone.urvoice.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _loginState = MutableStateFlow<ResultState<LoginResponse>>(ResultState.Initial)
    val loginState: StateFlow<ResultState<LoginResponse>> = _loginState

    fun login(email: String, password: String) {
        _loginState.value = ResultState.Loading

        viewModelScope.launch {
            try {
                val result = loginRepository.login(email, password)
                _loginState.value = result
            } catch (e: Exception) {
                _loginState.value = ResultState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun refreshSession(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = loginRepository.refreshSession(email, password)
                when (result) {
                    is ResultState.Success -> {
                        // Token automatically saved in repository
                    }
                    is ResultState.Error -> {
                        clearUserSession()
                    }
                    else -> {
                        // Handle other states if needed
                    }
                }
            } catch (e: Exception) {
                clearUserSession()
            }
        }
    }

    fun saveUserCredentials(email: String, password: String, token: String) {
        viewModelScope.launch {
            try {
                authPreferences.saveAuthToken(token)
            } catch (e: Exception) {
                _loginState.value = ResultState.Error("Failed to save credentials")
            }
        }
    }

    fun clearUserSession() {
        viewModelScope.launch {
            try {
                authPreferences.clearSession()
                _loginState.value = ResultState.Initial
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = ResultState.Initial
    }

    suspend fun safeApiCall(
        email: String,
        password: String,
        apiCall: suspend () -> Unit
    ) {
        try {
            val token = authPreferences.authToken.first()

            if (token.isNullOrEmpty()) {
                _loginState.value = ResultState.Error("No authentication token. Please login again.")
                return
            }

            apiCall()
        } catch (e: Exception) {
            when {
                e.message?.contains("401") == true ||
                        e.message?.contains("Unauthorized") == true -> {
                    try {
                        refreshSession(email, password)
                        apiCall()
                    } catch (refreshException: Exception) {
                        _loginState.value = ResultState.Error("Session expired. Please login again.")
                    }
                }
                else -> {
                    _loginState.value = ResultState.Error(e.message ?: "API call failed")
                }
            }
        }
    }

    suspend fun isUserLoggedIn(): Boolean {
        return loginRepository.isUserLoggedIn()
    }

    fun autoRefreshToken(email: String, password: String) {
        viewModelScope.launch {
            try {
                val token = authPreferences.authToken.first()
                if (!token.isNullOrEmpty()) {
                    refreshSession(email, password)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                loginRepository.logout()
                _loginState.value = ResultState.Initial
            } catch (e: Exception) {
                _loginState.value = ResultState.Error("Logout failed")
            }
        }
    }
}