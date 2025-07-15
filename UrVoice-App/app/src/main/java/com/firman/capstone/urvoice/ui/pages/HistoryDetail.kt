package com.firman.capstone.urvoice.ui.pages

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.*
import com.firman.capstone.urvoice.R
import com.firman.capstone.urvoice.utils.MediaUrlUtils
import com.firman.capstone.urvoice.ui.components.HistoryDetailAnalysisCard
import com.firman.capstone.urvoice.ui.components.SectionCard
import com.firman.capstone.urvoice.ui.theme.*
import com.firman.capstone.urvoice.utils.ResultState
import com.firman.capstone.urvoice.ui.viewmodel.HistoryViewModel
import com.simform.ssjetpackcomposeprogressbuttonlibrary.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    historyId: Int,
    onBackClick: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val historyDetailState by viewModel.historyDetailState.collectAsState()
    val context = LocalContext.current

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var audioButton by remember { mutableStateOf(SSButtonState.IDLE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(historyId) {
        viewModel.getHistoryById(historyId)
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.history_detail_title),
                        fontSize = 14.sp,
                        fontFamily = PoppinsSemiBold,
                        color = whiteColor
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = primaryColor),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close_white),
                            contentDescription = stringResource(R.string.back),
                            tint = whiteColor
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            when (val state = historyDetailState) {
                is ResultState.Loading, ResultState.Initial -> {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_animation))
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.Center)
                    )
                }

                is ResultState.Success -> {
                    val historyData = state.data
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 15.dp, vertical = 30.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = whiteColor),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = stringResource(R.string.audio_recording),
                                        fontSize = 14.sp,
                                        fontFamily = PoppinsSemiBold,
                                        color = textColor,
                                        modifier = Modifier.padding(bottom = 15.dp)
                                    )

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
                                                    historyData.fileAudio ?: "",
                                                    mediaPlayer,
                                                    isPlaying,
                                                    onMediaPlayerChange = { mediaPlayer = it },
                                                    onPlayingStateChange = {
                                                        isPlaying = it
                                                        audioButton =
                                                            if (it) SSButtonState.SUCCESS else SSButtonState.IDLE
                                                    },
                                                    onLoadingChange = {
                                                        audioButton =
                                                            if (it) SSButtonState.LOADING else SSButtonState.IDLE
                                                    },
                                                    onErrorChange = {
                                                        errorMessage = it
                                                        if (it != null) audioButton =
                                                            SSButtonState.FAILURE
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
                                            enabled = audioButton != SSButtonState.LOADING,
                                            text = when (audioButton) {
                                                SSButtonState.LOADING -> stringResource(R.string.loading_audio)
                                                SSButtonState.SUCCESS -> stringResource(R.string.pause_audio)
                                                SSButtonState.FAILURE -> stringResource(R.string.failed_play_audio)
                                                else -> stringResource(R.string.play_audio)
                                            }
                                        )
                                    }

                                    errorMessage?.let {
                                        Text(
                                            text = it,
                                            color = Color.Red,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            SectionCard(
                                title = stringResource(R.string.original_paragraph_title),
                                content = historyData.originalParagraph
                                    ?: stringResource(R.string.no_content_available)
                            )
                        }

                        item {
                            SectionCard(
                                title = stringResource(R.string.corrected_paragraph_title),
                                content = historyData.correctedParagraph
                                    ?: stringResource(R.string.no_correction_available)
                            )
                        }

                        if (!historyData.grammarAnalysis.isNullOrEmpty()) {
                            items(historyData.grammarAnalysis) { grammar ->
                                HistoryDetailAnalysisCard(grammar = grammar)
                            }
                        }
                    }
                }

                is ResultState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Terjadi kesalahan", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text(
                            state.errorMessage,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Button(
                            onClick = { viewModel.getHistoryById(historyId) },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Coba Lagi", color = whiteColor)
                        }
                    }
                }
            }
        }
    }
}

fun handlePlayPause(
    audioPath: String,
    mediaPlayer: MediaPlayer?,
    isPlaying: Boolean,
    onMediaPlayerChange: (MediaPlayer?) -> Unit,
    onPlayingStateChange: (Boolean) -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    onErrorChange: (String?) -> Unit
) {
    try {
        if (isPlaying) {
            mediaPlayer?.pause()
            onPlayingStateChange(false)
            return
        }

        onLoadingChange(true)
        onErrorChange(null)

        val fullAudioUrl = MediaUrlUtils.buildMediaUrl(audioPath)

        if (mediaPlayer == null) {
            val newMediaPlayer = MediaPlayer()
            newMediaPlayer.setDataSource(fullAudioUrl)
            newMediaPlayer.setOnPreparedListener {
                onLoadingChange(false)
                it.start()
                onPlayingStateChange(true)
            }
            newMediaPlayer.setOnCompletionListener {
                onPlayingStateChange(false)
                it.release()
                onMediaPlayerChange(null)
            }
            newMediaPlayer.setOnErrorListener { _, _, _ ->
                onLoadingChange(false)
                onErrorChange("Gagal memutar audio")
                onPlayingStateChange(false)
                onMediaPlayerChange(null)
                true
            }
            newMediaPlayer.prepareAsync()
            onMediaPlayerChange(newMediaPlayer)
        } else {
            mediaPlayer.start()
            onPlayingStateChange(true)
            onLoadingChange(false)
        }
    } catch (e: Exception) {
        Log.e("AudioPlayer", "Playback error", e)
        onErrorChange("Gagal memutar audio: ${e.message}")
        onPlayingStateChange(false)
        onLoadingChange(false)
    }
}
