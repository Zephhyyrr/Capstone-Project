package com.firman.capstone.urvoice.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.firman.capstone.urvoice.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.firman.capstone.urvoice.data.remote.models.HistoryResponse
import com.firman.capstone.urvoice.ui.theme.*

@Composable
fun HistoryDetailAnalysisCard(
    grammar: HistoryResponse.GrammarAnalysis,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = whiteColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.grammar_analysis_title),
                fontSize = 12.sp,
                fontFamily = PoppinsSemiBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            AnalysisItem(label = stringResource(R.string.original), text = grammar.original)
            Spacer(modifier = Modifier.height(8.dp))

            AnalysisItem(label = stringResource(R.string.corrected), text = grammar.corrected, highlight = true)
            Spacer(modifier = Modifier.height(8.dp))

            AnalysisItem(label = stringResource(R.string.reason), text = grammar.reason)
        }
    }
}

@Composable
private fun AnalysisItem(
    label: String,
    text: String?,
    highlight: Boolean = false
) {
    Text(
        text = "$label:",
        fontSize = 12.sp,
        color = textColor,
        fontFamily = PoppinsSemiBold,
    )
    Text(
        text = text ?: "No $label provided",
        fontSize = 12.sp,
        fontFamily = PoppinsRegular,
        color = if (highlight) textColor else textColor,
        modifier = Modifier.padding(top = 2.dp)
    )
}
