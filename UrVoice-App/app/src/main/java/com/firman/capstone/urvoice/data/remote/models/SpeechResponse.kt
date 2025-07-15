package com.firman.capstone.urvoice.data.remote.models

import com.google.gson.annotations.SerializedName

data class SpeechResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: Data? = null
) {
    data class Data(
        @SerializedName("text")
        val text: String? = null,

        @SerializedName("audioFileName")
        val audioFileName: String? = null,

        @SerializedName("audioPath")
        val audioPath: String? = null
    )
}