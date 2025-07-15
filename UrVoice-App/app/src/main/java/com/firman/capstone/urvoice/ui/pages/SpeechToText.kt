package com.firman.capstone.urvoice.ui.pages

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.firman.capstone.urvoice.R
import com.firman.capstone.urvoice.ui.components.SpeechToTextCard
import com.firman.capstone.urvoice.ui.theme.PoppinsSemiBold
import com.firman.capstone.urvoice.ui.theme.UrVoiceTheme
import com.firman.capstone.urvoice.ui.theme.primaryColor
import com.firman.capstone.urvoice.ui.theme.whiteBackground
import com.firman.capstone.urvoice.ui.theme.whiteColor
import com.firman.capstone.urvoice.ui.viewmodel.SpeechViewModel
import com.firman.capstone.urvoice.utils.ResultState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonType
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSJetPackComposeProgressButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeechToTextScreen(
    modifier: Modifier = Modifier,
    viewModel: SpeechViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateRecordScreen: () -> Unit = {},
    onNavigateAnalyzeScreen: (String, String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var retryRecordStateButton by remember { mutableStateOf(SSButtonState.IDLE) }
    var analyzeStateButton by remember { mutableStateOf(SSButtonState.IDLE) }

    val convertedText by viewModel.convertedText.collectAsStateWithLifecycle()
    val audioFileName by viewModel.audioFileName.collectAsStateWithLifecycle()
    val speechToTextState by viewModel.speechToTextState.collectAsStateWithLifecycle()

    // Handle speech to text state changes
    LaunchedEffect(speechToTextState) {
        when (speechToTextState) {
            is ResultState.Success -> {
                Log.d("SpeechToTextScreen", "Speech to text success with filename: $audioFileName")
            }

            is ResultState.Error -> {
                Log.e(
                    "SpeechToTextScreen",
                    "Speech to text error: ${(speechToTextState as ResultState.Error).errorMessage}"
                )
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.speech_to_text_title),
                        fontSize = 14.sp,
                        fontFamily = PoppinsSemiBold,
                        color = whiteColor
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = primaryColor
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetState()
                        onBackClick()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close_white),
                            contentDescription = stringResource(R.string.back),
                            tint = whiteColor
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SpeechToTextCard(
                        text = convertedText.ifEmpty { stringResource(R.string.example_speech_to_text) },
                        isLoading = speechToTextState is ResultState.Loading
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(start = 15.dp, end = 15.dp, bottom = 15.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SSJetPackComposeProgressButton(
                        type = SSButtonType.CIRCLE,
                        width = 157.dp,
                        height = 50.dp,
                        buttonBorderColor = primaryColor,
                        buttonBorderWidth = 2.dp,
                        buttonState = retryRecordStateButton,
                        onClick = {
                            coroutineScope.launch {
                                retryRecordStateButton = SSButtonState.LOADING
                                viewModel.resetState()
                                delay(500)
                                retryRecordStateButton = SSButtonState.IDLE
                                onNavigateRecordScreen()
                            }
                        },
                        cornerRadius = 100,
                        assetColor = primaryColor,
                        successIconPainter = null,
                        failureIconPainter = null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = whiteBackground,
                            contentColor = primaryColor
                        ),
                        text = stringResource(R.string.button_retry_record),
                        fontSize = 14.sp,
                        fontFamily = PoppinsSemiBold
                    )

                    SSJetPackComposeProgressButton(
                        type = SSButtonType.CIRCLE,
                        width = 157.dp,
                        height = 50.dp,
                        buttonState = analyzeStateButton,
                        onClick = {
                            coroutineScope.launch {
                                analyzeStateButton = SSButtonState.LOADING

                                val textToAnalyze = convertedText
                                val currentAudioFileName = audioFileName

                                if (textToAnalyze.isNotEmpty() && currentAudioFileName.isNotEmpty()) {
                                    analyzeStateButton = SSButtonState.SUCCESS
                                    onNavigateAnalyzeScreen(textToAnalyze, currentAudioFileName)
                                } else {
                                    analyzeStateButton = SSButtonState.FAILURE
                                    delay(2000)
                                    analyzeStateButton = SSButtonState.IDLE
                                }
                            }
                        },
                        cornerRadius = 100,
                        assetColor = whiteColor,
                        successIconPainter = null,
                        failureIconPainter = null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = whiteColor,
                            disabledContainerColor = primaryColor,
                        ),
                        enabled = convertedText.isNotEmpty() && audioFileName.isNotEmpty() && analyzeStateButton == SSButtonState.IDLE,
                        text = stringResource(R.string.button_analyze_grammar),
                        fontSize = 14.sp,
                        fontFamily = PoppinsSemiBold
                    )
                }
            }
        }
    }
}