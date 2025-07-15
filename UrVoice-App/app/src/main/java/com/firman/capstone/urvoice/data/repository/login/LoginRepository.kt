package com.firman.capstone.urvoice.data.repository.login

import com.firman.capstone.urvoice.data.remote.models.LoginResponse
import com.firman.capstone.urvoice.utils.ResultState

interface LoginRepository {
    suspend fun login(email: String, password: String): ResultState<LoginResponse>
    suspend fun refreshSession(email: String, password: String): ResultState<Unit>
    suspend fun isUserLoggedIn(): Boolean
    suspend fun logout(): ResultState<Unit>
}