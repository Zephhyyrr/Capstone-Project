package com.firman.capstone.urvoice.data.repository.register

import com.firman.capstone.urvoice.data.remote.models.RegisterResponse
import com.firman.capstone.urvoice.utils.ResultState

interface RegisterRepository {
    suspend fun register(name: String, email: String, password: String): ResultState<RegisterResponse>
}