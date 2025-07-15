package com.firman.capstone.urvoice.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.firman.capstone.urvoice.R
import com.firman.capstone.urvoice.data.remote.models.AnalyzeResponse
import com.firman.capstone.urvoice.ui.theme.PoppinsRegular
import com.firman.capstone.urvoice.ui.theme.PoppinsSemiBold
import com.firman.capstone.urvoice.ui.theme.textColor
import com.firman.capstone.urvoice.ui.theme.whiteColor

@Composable
fun SectionCard(
    title: String,
    content: String,
    backgroundColor: Color = whiteColor,
    borderColor: Color? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (borderColor != null) 0.dp else 2.dp
        ),
        border = borderColor?.let {
            BorderStroke(2.dp, it)
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontFamily = PoppinsSemiBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = content,
                fontSize = 14.sp,
                fontFamily = PoppinsRegular,
                color = textColor,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun GrammarAnalysisCard(
    title: String,
    grammar: AnalyzeResponse.GrammarAnalysis,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontFamily = PoppinsSemiBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextLabelValue(
                label = stringResource(R.string.original),
                value = grammar.original ?: stringResource(R.string.no_original_text)
            )

            TextLabelValue(
                label = stringResource(R.string.corrected),
                value = grammar.corrected ?: stringResource(R.string.no_correction_available)
            )

            // Reason
            TextLabelValue(
                label = stringResource(R.string.reason),
                value = grammar.reason ?: stringResource(R.string.no_reason_text),
                isLast = true
            )
        }
    }
}

@Composable
private fun TextLabelValue(label: String, value: String, isLast: Boolean = false) {
    Text(
        text = label,
        fontSize = 12.sp,
        fontFamily = PoppinsSemiBold,
        color = textColor
    )
    Text(
        text = value,
        fontSize = 12.sp,
        color = textColor,
        fontFamily = PoppinsRegular,
        modifier = Modifier.padding(bottom = if (isLast) 0.dp else 12.dp),
        lineHeight = 20.sp
    )
}