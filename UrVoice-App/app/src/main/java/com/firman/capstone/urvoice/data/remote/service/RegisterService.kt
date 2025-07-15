package com.firman.capstone.urvoice.data.remote.service

import com.firman.capstone.urvoice.data.remote.request.RegisterRequest
import com.firman.capstone.urvoice.data.remote.models.RegisterResponse
import com.firman.capstone.urvoice.utils.ApiConstant
import retrofit2.http.Body
import retrofit2.http.POST

interface RegisterService {
    @POST(ApiConstant.USER_REGISTER)
    suspend fun register(@Body request: RegisterRequest): RegisterResponse
}
