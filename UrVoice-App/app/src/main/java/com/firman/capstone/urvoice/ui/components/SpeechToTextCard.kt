package com.firman.capstone.urvoice.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.firman.capstone.urvoice.ui.theme.PoppinsRegular
import com.firman.capstone.urvoice.ui.theme.textColor
import com.firman.capstone.urvoice.ui.theme.whiteColor

@Composable
fun SpeechToTextCard(
    text: String,
    modifier: Modifier = Modifier,
    elevation: Int = 2,
    isLoading: Boolean
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 15.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = whiteColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation.dp
        )
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            style = TextStyle(
                fontFamily = PoppinsRegular,
                color = textColor,
                fontSize = 16.sp
            )
        )
    }
}