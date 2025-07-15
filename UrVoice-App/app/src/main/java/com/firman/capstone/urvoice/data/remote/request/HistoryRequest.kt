package com.firman.capstone.urvoice.data.remote.request

import com.google.gson.annotations.SerializedName

data class HistoryRequest(
    @SerializedName("audioFileName")
    val audioFileName: String,

    @SerializedName("originalParagraph")
    val originalParagraph: String,

    @SerializedName("correctedParagraph")
    val correctedParagraph: String,

    @SerializedName("grammarAnalysis")
    val grammarAnalysis: List<GrammarAnalysis>
) {
    data class GrammarAnalysis(
        @SerializedName("corrected")
        val corrected: String,

        @SerializedName("original")
        val original: String,

        @SerializedName("reason")
        val reason: String
    )
}
