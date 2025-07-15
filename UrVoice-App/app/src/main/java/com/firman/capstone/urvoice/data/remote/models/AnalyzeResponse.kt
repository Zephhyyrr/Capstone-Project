package com.firman.capstone.urvoice.data.remote.models

import com.google.gson.annotations.SerializedName

data class AnalyzeResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: Data? = null
) {
    data class Data(
        @SerializedName("corrected_paragraph")
        val correctedParagraph: String? = null,

        @SerializedName("grammar_analysis")
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
