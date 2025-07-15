package com.firman.capstone.urvoice.data.remote.service

import com.firman.capstone.urvoice.data.remote.models.HistoryDetailResponse
import com.firman.capstone.urvoice.data.remote.models.HistoryResponse
import com.firman.capstone.urvoice.data.remote.models.SaveHistoryResponse
import com.firman.capstone.urvoice.data.remote.request.HistoryRequest
import com.firman.capstone.urvoice.utils.ApiConstant
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface HistoryService {

    @POST(ApiConstant.SAVE_HISTORY)
    suspend fun saveHistory(
        @Header("Authorization") authorization: String,
        @Body request: HistoryRequest
    ): SaveHistoryResponse

    @GET(ApiConstant.HISTORY_LIST)
    suspend fun getAllHistory(
        @Header("Authorization") authorization: String
    ): HistoryResponse

    @GET(ApiConstant.HISTORY_DETAIL)
    suspend fun getHistoryById(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): HistoryDetailResponse
}