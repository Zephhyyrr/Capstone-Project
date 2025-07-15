package com.firman.capstone.urvoice.data.remote.models

import com.google.gson.annotations.SerializedName

data class ArticleResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: List<Data>? = null
) {
    data class Data(
        @SerializedName("id")
        val id: Int? = null,

        @SerializedName("title")
        val title: String? = null,

        @SerializedName("content")
        val content: String? = null,

        @SerializedName("imageUrl")
        val image: String? = null,

        @SerializedName("urlArticle")
        val urlArticle: String? = null,
    )

    data class meta(
        @SerializedName("page")
        val page: Int? = null,

        @SerializedName("size")
        val size: Int? = null,

        @SerializedName("totalItems")
        val totalItems: Int? = null
    )
}