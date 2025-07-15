package com.firman.capstone.urvoice.data.remote.models

import com.google.gson.annotations.SerializedName

data class SaveHistoryResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: HistoryResponse.Data? = null
)
