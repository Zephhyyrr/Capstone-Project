package com.firman.capstone.urvoice.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firman.capstone.urvoice.data.local.datastore.AuthPreferences
import com.firman.capstone.urvoice.data.local.datastore.OnboardingPreferences
import com.firman.capstone.urvoice.data.repository.login.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val onboardingPreferences: OnboardingPreferences,
    private val loginRepository: LoginRepository
) : ViewModel() {

    data class AppStartupState(
        val isOnboardingCompleted: Boolean = false,
        val isLoggedIn: Boolean = false,
        val isLoading: Boolean = true
    )

    private val _isLoading = MutableStateFlow(true)

    val appStartupState: StateFlow<AppStartupState> = combine(
        onboardingPreferences.hasSeenOnboarding,
        authPreferences.authToken,
        _isLoading
    ) { hasSeenOnboarding, token, isLoading ->
        Log.d("SplashDebug", "Onboarding: $hasSeenOnboarding, Token: $token, Loading: $isLoading")

        AppStartupState(
            isOnboardingCompleted = hasSeenOnboarding,
            isLoggedIn = !token.isNullOrEmpty(),
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppStartupState(isLoading = true)
    )

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            try {
                // Give some time for data to load properly
                kotlinx.coroutines.delay(500)

                // Validate login status if token exists
                val token = authPreferences.authToken.first()
                if (!token.isNullOrEmpty()) {
                    val isValidLogin = loginRepository.isUserLoggedIn()
                    if (!isValidLogin) {
                        // Clear invalid token
                        authPreferences.clearSession()
                        Log.d("SplashDebug", "Invalid token cleared")
                    }
                }

                _isLoading.value = false
                Log.d("SplashDebug", "App initialization completed")
            } catch (e: Exception) {
                Log.e("SplashDebug", "Error during app initialization", e)
                _isLoading.value = false
            }
        }
    }

    fun refreshAppState() {
        _isLoading.value = true
        initializeApp()
    }
}