package com.firman.capstone.urvoice.ui.components

import android.media.MediaPlayer
import com.firman.capstone.urvoice.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.firman.capstone.urvoice.ui.theme.PoppinsRegular
import com.firman.capstone.urvoice.ui.theme.PoppinsSemiBold
import com.firman.capstone.urvoice.ui.theme.primaryColor
import com.firman.capstone.urvoice.ui.theme.textColor
import com.firman.capstone.urvoice.ui.theme.whiteColor
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonType
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSJetPackComposeProgressButton

@Composable
fun HistoryCard(
    audioFileName: String,
    correctedParagraph: String,
    dayMonth: String,
    year: String,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    rememberCoroutineScope()
    var audioButton by remember { mutableStateOf(SSButtonState.IDLE) }
    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.release()
            } catch (_: Exception) {
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp, top = 4.dp)
        ) {
            Text(
                text = dayMonth,
                fontSize = 14.sp,
                fontFamily = PoppinsSemiBold,
                color = textColor
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = year,
                fontSize = 14.sp,
                fontFamily = PoppinsSemiBold,
                color = textColor
            )

            Spacer(modifier = Modifier.width(8.dp))
            Divider(
                modifier = Modifier
                    .weight(2f)
                    .width(173.dp)
                    .padding(start = 48.dp)
                    .height(1.dp),
                color = textColor
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            )
            {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(15.dp)
                    ) {

                        Text(
                            text = stringResource(R.string.corrected_paragraph_title),
                            fontSize = 14.sp,
                            fontFamily = PoppinsSemiBold,
                            color = textColor,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Text(
                            text = correctedParagraph,
                            fontSize = 12.sp,
                            color = textColor,
                            lineHeight = 18.sp,
                            fontFamily = PoppinsRegular,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                fontSize = 12.sp,
                                color = Color.Red,
                                fontFamily = PoppinsRegular,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            SSJetPackComposeProgressButton(
                                type = SSButtonType.CIRCLE,
                                width = 323.dp,
                                height = 45.dp,
                                buttonBorderColor = primaryColor,
                                buttonBorderWidth = 2.dp,
                                buttonState = audioButton,
                                onClick = {
                                    handlePlayPause(
                                        context = context,
                                        audioPath = audioFileName,
                                        mediaPlayer = mediaPlayer,
                                        isPlaying = isPlaying,
                                        onMediaPlayerChange = { mediaPlayer = it },
                                        onPlayingStateChange = { playing ->
                                            isPlaying = playing
                                            audioButton =
                                                if (playing) SSButtonState.SUCCESS else SSButtonState.IDLE
                                        },
                                        onLoadingChange = { isLoading ->
                                            audioButton =
                                                if (isLoading) SSButtonState.LOADING else SSButtonState.IDLE
                                        },
                                        onErrorChange = { error ->
                                            errorMessage = error
                                            if (error != null) audioButton = SSButtonState.FAILURE
                                        }
                                    )
                                },
                                cornerRadius = 100,
                                successIconPainter = null,
                                failureIconPainter = null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor,
                                    contentColor = whiteColor,
                                    disabledContainerColor = primaryColor,
                                ),
                                assetColor = Color.White,
                                enabled = audioButton == SSButtonState.IDLE || audioButton == SSButtonState.SUCCESS,
                                text = when (audioButton) {
                                    SSButtonState.LOADING -> stringResource(R.string.loading_audio)
                                    SSButtonState.SUCCESS -> stringResource(R.string.pause_audio)
                                    SSButtonState.FAILURE -> stringResource(R.string.failed_play_audio)
                                    else -> stringResource(R.string.play_audio)
                                }
                            )
                        }
                    }
                    Icon(
                        painter = painterResource(R.drawable.arrow_right),
                        contentDescription = stringResource(R.string.go_detail_description),
                        tint = textColor,
                        modifier = Modifier
                            .size(30.dp)
                            .padding(start = 4.dp, end = 8.dp)
                    )
                }
            }
        }
    }
}
