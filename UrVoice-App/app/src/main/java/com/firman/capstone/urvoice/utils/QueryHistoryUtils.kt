package com.firman.capstone.urvoice.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.firman.capstone.urvoice.data.remote.models.HistoryResponse
import java.time.Instant
import java.time.format.DateTimeParseException

object QueryHistoryUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getLatestHistory(historyList: List<HistoryResponse.Data>): HistoryResponse.Data? {
        return historyList
            .filter { it.createdAt != null }
            .maxByOrNull {
                try {
                    Instant.parse(it.createdAt)
                } catch (e: DateTimeParseException) {
                    Instant.MIN
                }
            }
    }
}
