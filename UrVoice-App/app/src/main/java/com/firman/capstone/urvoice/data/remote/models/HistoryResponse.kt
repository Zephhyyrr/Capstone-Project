package com.firman.capstone.urvoice.data.remote.models

import com.google.gson.annotations.SerializedName

data class HistoryResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: List<Data>? = null
) {
    data class Data(
        @SerializedName("id")
        val id: Int,

        @SerializedName("fileAudio")
        val fileAudio: String? = null,

        @SerializedName("originalParagraph")
        val originalParagraph: String? = null,

        @SerializedName("correctedParagraph")
        val correctedParagraph: String? = null,

        @SerializedName("createdAt")
        val createdAt: String? = null,

        @SerializedName("updatedAt")
        val updatedAt: String? = null,

        @SerializedName("grammarAnalysis")
        val grammarAnalysis: List<GrammarAnalysis>? = null
    )

    data class GrammarAnalysis(
        @SerializedName("corrected")
        val corrected: String? = null,

        @SerializedName("original")
        val original: String? = null,

        @SerializedName("reason")
        val reason: String? = null
    )
}
