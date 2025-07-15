package com.firman.capstone.urvoice.data.remote.service

import com.firman.capstone.urvoice.data.remote.request.LoginRequest
import com.firman.capstone.urvoice.data.remote.models.LoginResponse
import com.firman.capstone.urvoice.utils.ApiConstant
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {

    @POST(ApiConstant.USER_LOGIN)
    suspend fun login(@Body request: LoginRequest): LoginResponse
}