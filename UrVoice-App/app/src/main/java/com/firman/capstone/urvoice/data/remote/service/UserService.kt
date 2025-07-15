package com.firman.capstone.urvoice.data.remote.service

import com.firman.capstone.urvoice.data.remote.models.CurrentUserResponse
import com.firman.capstone.urvoice.data.remote.models.DeleteUserResponse
import com.firman.capstone.urvoice.data.remote.models.UserLogoutResponse
import com.firman.capstone.urvoice.data.remote.models.UserProfileResponse
import com.firman.capstone.urvoice.data.remote.models.UserResponse
import com.firman.capstone.urvoice.data.remote.request.UpdateUserRequest
import com.firman.capstone.urvoice.utils.ApiConstant
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UserService {

    @GET(ApiConstant.USER_CURRENT)
    suspend fun getCurrentUser(@Header("Authorization") token: String): CurrentUserResponse

    @PUT(ApiConstant.USER_UPDATE)
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Body request: UpdateUserRequest
    ): UserResponse

    @Multipart
    @POST(ApiConstant.USER_PROFILE)
    suspend fun getUserProfile(
        @Header("Authorization") token: String,
        @Part profileImage: MultipartBody.Part
    ): Response<UserProfileResponse>

    @POST(ApiConstant.USER_LOGOUT)
    suspend fun logout(@Header("Authorization") token: String): UserLogoutResponse

    @DELETE(ApiConstant.USER_DELETE)
    suspend fun deleteUser(@Header("Authorization") token: String): DeleteUserResponse
}
