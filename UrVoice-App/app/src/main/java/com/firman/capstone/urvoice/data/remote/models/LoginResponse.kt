package com.firman.capstone.urvoice.data.remote.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: LoginData? = null,
    val refreshToken: String
) {
    data class LoginData(
        @SerializedName("email")
        val email: String? = null,

        @SerializedName("accessToken")
        val accessToken: String? = null,

        @SerializedName("refreshToken")
        val refreshToken: String? = null
    )
}


