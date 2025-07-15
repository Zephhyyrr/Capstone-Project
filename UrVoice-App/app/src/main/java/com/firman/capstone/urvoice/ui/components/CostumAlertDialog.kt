package com.firman.capstone.urvoice.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.firman.capstone.urvoice.ui.theme.*

@Composable
fun CustomAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    positiveText: String = "",
    negativeText: String = "",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = modifier
            .width(280.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontFamily = PoppinsSemiBold,
                    color = textColor,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    fontFamily = PoppinsRegular,
                    color = textColor,
                    textAlign = TextAlign.Start,
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = negativeText,
                        fontFamily = PoppinsSemiBold,
                        color = primaryColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onConfirm) {
                    Text(
                        text = positiveText,
                        fontFamily = PoppinsSemiBold,
                        color = primaryColor
                    )
                }
            }
        }
    }
}
