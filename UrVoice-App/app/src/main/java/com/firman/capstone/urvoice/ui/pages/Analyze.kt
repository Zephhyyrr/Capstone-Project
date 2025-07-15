package com.firman.capstone.urvoice.ui.pages

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.*
import com.firman.capstone.urvoice.R
import com.firman.capstone.urvoice.data.remote.models.AnalyzeResponse
import com.firman.capstone.urvoice.data.remote.request.HistoryRequest
import com.firman.capstone.urvoice.ui.components.AudioPlayerCard
import com.firman.capstone.urvoice.ui.components.GrammarAnalysisCard
import com.firman.capstone.urvoice.ui.components.SectionCard
import com.firman.capstone.urvoice.ui.theme.PoppinsSemiBold
import com.firman.capstone.urvoice.ui.theme.primaryColor
import com.firman.capstone.urvoice.ui.theme.whiteBackground
import com.firman.capstone.urvoice.ui.theme.whiteColor
import com.firman.capstone.urvoice.utils.ResultState
import com.firman.capstone.urvoice.ui.viewmodel.AnalyzeViewModel
import com.firman.capstone.urvoice.ui.viewmodel.HistoryViewModel
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonType
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSJetPackComposeProgressButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeScreen(
    modifier: Modifier = Modifier,
    text: String,
    audioFileName: String,
    onBackClick: () -> Unit,
    onNavigateToHistory:  () -> Unit = {},
    viewModel: AnalyzeViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel = hiltViewModel()
) {
    val analyzeState by viewModel.analyzeState.collectAsState()
    val saveHistoryState by historyViewModel.saveHistoryState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var retryRecordStateButton by remember { mutableStateOf(SSButtonState.IDLE) }
    var analyzeStateButton by remember { mutableStateOf(SSButtonState.IDLE) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(saveHistoryState) {

        when (saveHistoryState) {
            is ResultState.Success -> {
                analyzeStateButton = SSButtonState.SUCCESS
                isSaving = false
                coroutineScope.launch {
                    delay(1000)
                    onNavigateToHistory()
                }
            }
            is ResultState.Error -> {
                analyzeStateButton = SSButtonState.FAILURE
                isSaving = false
                delay(2000)
                analyzeStateButton = SSButtonState.IDLE
            }
            is ResultState.Loading -> {
                Log.d("AnalyzeScreen", "Save in progress...")
            }
            else -> {
                Log.d("AnalyzeScreen", "Save state: Initial/Other")
            }
        }
    }

    LaunchedEffect(text, audioFileName) {
        viewModel.setInputData(text, audioFileName)
        viewModel.analyzeText()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.analisis_title),
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
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            when (val state = analyzeState) {
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
                    val data = state.data.data
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            AnalyzeContent(
                                originalText = text,
                                audioFileName = audioFileName,
                                analyzeData = data
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
                                        historyViewModel.resetSaveState()
                                        delay(500)
                                        retryRecordStateButton = SSButtonState.IDLE
                                        onBackClick()
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
                                    if (data != null && !isSaving) {
                                        analyzeStateButton = SSButtonState.LOADING
                                        isSaving = true

                                        val grammarAnalysis = data.grammarAnalysis?.map { grammar ->
                                            HistoryRequest.GrammarAnalysis(
                                                corrected = grammar.corrected ?: "",
                                                original = grammar.original ?: "",
                                                reason = grammar.reason ?: ""
                                            )
                                        } ?: emptyList()

                                        historyViewModel.saveHistory(
                                            audioFileName = audioFileName,
                                            originalParagraph = text,
                                            correctedParagraph = data.correctedParagraph ?: "",
                                            grammarAnalysis = grammarAnalysis
                                        )
                                    }
                                },
                                cornerRadius = 100,
                                assetColor = whiteColor,
                                successIconPainter = null,
                                failureIconPainter = null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor,
                                    contentColor = whiteColor,
                                    disabledContainerColor = primaryColor.copy(alpha = 0.6f),
                                ),
                                enabled = !isSaving,
                                text = when (analyzeStateButton) {
                                    SSButtonState.LOADING -> stringResource(R.string.saving_progress)
                                    SSButtonState.SUCCESS -> stringResource(R.string.saved)
                                    SSButtonState.FAILURE -> stringResource(R.string.failed_save)
                                    else -> stringResource(R.string.button_save_analyze)
                                },
                                fontSize = 14.sp,
                                fontFamily = PoppinsSemiBold
                            )
                        }
                    }
                }

                is ResultState.Error -> {
                    ErrorState(
                        message = state.errorMessage,
                        onRetry = { viewModel.retryAnalysis() }
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyzeContent(
    originalText: String,
    audioFileName: String,
    analyzeData: AnalyzeResponse.Data?
) {
    LaunchedEffect(audioFileName) {
        Log.d("AnalyzeContent", "Passing audioFileName to AudioPlayerCard: $audioFileName")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AudioPlayerCard(
                audioPath = audioFileName,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            SectionCard(
                title = stringResource(R.string.original_paragraph_title),
                content = originalText
            )
        }

        item {
            SectionCard(
                title = stringResource(R.string.corrected_paragraph_title),
                content = analyzeData?.correctedParagraph
                    ?: stringResource(R.string.no_correction_available),
            )
        }

        if (!analyzeData?.grammarAnalysis.isNullOrEmpty()) {
            items(analyzeData.grammarAnalysis) { grammar ->
                GrammarAnalysisCard(
                    title = stringResource(R.string.grammar_analysis_title),
                    grammar = grammar
                )
            }
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.error_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text(stringResource(R.string.retry), color = whiteColor)
            }
        }
    }
}