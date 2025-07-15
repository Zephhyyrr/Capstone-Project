package com.firman.capstone.urvoice.ui.components

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.firman.capstone.urvoice.R
import com.firman.capstone.urvoice.ui.theme.*
import com.firman.capstone.urvoice.utils.MediaUrlUtils
import java.io.File

@Composable
fun AudioPlayerCard(
    audioPath: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.release()
            } catch (_: Exception) {
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = whiteColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.rekaman_audio),
                fontSize = 14.sp,
                fontFamily = PoppinsSemiBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp)
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

            Button(
                onClick = {
                    handlePlayPause(
                        context = context,
                        audioPath = audioPath,
                        mediaPlayer = mediaPlayer,
                        isPlaying = isPlaying,
                        onMediaPlayerChange = { mediaPlayer = it },
                        onPlayingStateChange = { isPlaying = it },
                        onLoadingChange = { isLoading = it },
                        onErrorChange = { errorMessage = it }
                    )
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = whiteColor,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Loading...", color = whiteColor, fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = if (isPlaying) "⏸ Pause Audio" else "▶ Play Audio",
                        color = whiteColor
                    )
                }
            }
        }
    }
}

internal fun handlePlayPause(
    context: Context,
    audioPath: String,
    mediaPlayer: MediaPlayer?,
    isPlaying: Boolean,
    onMediaPlayerChange: (MediaPlayer?) -> Unit,
    onPlayingStateChange: (Boolean) -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    onErrorChange: (String?) -> Unit
) {
    try {
        if (mediaPlayer == null) {
            onLoadingChange(true)
            onErrorChange(null)

            val newMediaPlayer = MediaPlayer()

            newMediaPlayer.setOnPreparedListener { mp ->
                onLoadingChange(false)
                mp.start()
                onPlayingStateChange(true)
            }

            newMediaPlayer.setOnCompletionListener {
                onPlayingStateChange(false)
            }

            newMediaPlayer.setOnErrorListener { _, _, _ ->
                onErrorChange("Error playing audio file")
                onLoadingChange(false)
                onPlayingStateChange(false)
                true
            }

            val audioSource = getAudioSource(context, audioPath)
            if (audioSource == null) {
                onErrorChange("Audio file not found.")
                onLoadingChange(false)
                newMediaPlayer.release()
                return
            }

            newMediaPlayer.setDataSource(audioSource)
            onMediaPlayerChange(newMediaPlayer)
            newMediaPlayer.prepareAsync()

        } else {
            if (isPlaying) {
                mediaPlayer.pause()
                onPlayingStateChange(false)
            } else {
                mediaPlayer.start()
                onPlayingStateChange(true)
            }
        }
    } catch (e: Exception) {
        onErrorChange("Playback error: ${e.message}")
        onLoadingChange(false)
    }
}

private fun getAudioSource(context: Context, audioPath: String): String? {
    val mediaUrl = MediaUrlUtils.buildMediaUrl(audioPath)
    if (mediaUrl.isNotEmpty() && (mediaUrl.startsWith("http://") || mediaUrl.startsWith("https://"))) {
        return mediaUrl
    }

    val possiblePaths = listOf(
        File(audioPath),
        File(context.cacheDir, audioPath),
        File(context.filesDir, audioPath),
        File(context.getExternalFilesDir(null), audioPath),
        File(context.cacheDir, File(audioPath).name),
        File(context.filesDir, File(audioPath).name)
    )

    return possiblePaths.firstOrNull { it.exists() }?.absolutePath
}
