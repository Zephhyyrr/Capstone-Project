package com.firman.capstone.urvoice.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.firman.capstone.urvoice.R
import com.firman.capstone.urvoice.ui.theme.PoppinsRegular
import com.firman.capstone.urvoice.ui.theme.PoppinsSemiBold
import com.firman.capstone.urvoice.ui.theme.UrVoiceTheme
import com.firman.capstone.urvoice.ui.theme.textColor
import com.firman.capstone.urvoice.ui.theme.whiteColor
import com.firman.capstone.urvoice.utils.MediaUrlUtils

@Composable
fun CardArticle(title: String, content: String, imageUrl: String, onClick: () -> Unit) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = whiteColor),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(380.dp)
            .height(120.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            SubcomposeAsyncImage(
                model = MediaUrlUtils.buildMediaUrl(imageUrl),
                contentDescription = stringResource(R.string.article_image_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp),
                loading = {
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(end = 24.dp)
                ) {
                    Text(
                        text = title,
                        fontFamily = PoppinsSemiBold,
                        fontSize = 12.sp,
                        color = textColor,
                        maxLines = 1,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = content,
                        fontSize = 10.sp,
                        maxLines = 2,
                        color = textColor,
                        fontFamily = PoppinsRegular,
                    )
                }

                Image(
                    painter = painterResource(R.drawable.arrow_right),
                    contentDescription = stringResource(R.string.article_arrow_desc),
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterEnd),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardArticlePreview() {
    UrVoiceTheme {
        CardArticle(
            title = "Sample Article Title",
            content = "This is a sample content for the article. It should be concise and informative.",
            imageUrl = "https://example.com/sample-image.jpg",
            onClick = { /* Preview onClick */ },
        )
    }
}