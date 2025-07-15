package com.firman.capstone.urvoice.data.repository.register

import com.firman.capstone.urvoice.data.remote.models.RegisterResponse
import com.firman.capstone.urvoice.data.remote.request.RegisterRequest
import com.firman.capstone.urvoice.data.remote.service.RegisterService
import com.firman.capstone.urvoice.utils.ResultState
import javax.inject.Inject

class RegisterRepositoryImpl @Inject constructor(
    private val registerService: RegisterService
) : RegisterRepository {
    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): ResultState<RegisterResponse> {
        return try {
            val request = RegisterRequest(name, email, password)
            val response = registerService.register(request)
            if (response.success) {
                ResultState.Success(response)
            } else {
                ResultState.Error(response.message ?: "Register failed")
            }
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Unknown error occurred")
        }
    }
}
