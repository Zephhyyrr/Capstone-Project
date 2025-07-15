package com.firman.capstone.urvoice.data.remote.models

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: RegisterData
) {
    data class RegisterData(
        @SerializedName("name")
        val name: String,

        @SerializedName("email")
        val email: String,

        @SerializedName("password")
        val password: String
    )
}
