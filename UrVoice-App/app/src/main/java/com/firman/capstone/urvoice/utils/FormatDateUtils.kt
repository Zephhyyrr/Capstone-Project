package com.firman.capstone.urvoice.utils

import java.text.SimpleDateFormat
import java.util.*

object FormatDateUtils {

    fun formatDate(createdAt: String?): Pair<String, String> {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")

            val date = parser.parse(createdAt ?: "") ?: return "" to ""

            val dayMonthFormat = SimpleDateFormat("dd MMM", Locale("id"))
            val yearFormat = SimpleDateFormat("yyyy", Locale("id"))

            val dayMonth = dayMonthFormat.format(date)
            val year = yearFormat.format(date)

            dayMonth to year
        } catch (e: Exception) {
            "" to ""
        }
    }
}
