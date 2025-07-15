package com.firman.capstone.urvoice.ui.pages

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.firman.capstone.urvoice.R
import com.firman.capstone.urvoice.ui.theme.*
import com.firman.capstone.urvoice.ui.viewmodel.ArticleViewModel
import com.firman.capstone.urvoice.utils.ResultState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonType
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSJetPackComposeProgressButton
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    viewModel: ArticleViewModel,
    articleId: Int,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val articleState by viewModel.articleDetail.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var linkButtonState by remember { mutableStateOf(SSButtonState.IDLE) }
    val context = LocalContext.current

    LaunchedEffect(articleId) {
        viewModel.getArticleById(articleId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.article_detail_title),
                        fontSize = 14.sp,
                        fontFamily = PoppinsSemiBold,
                        color = whiteColor
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = primaryColor
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close_white),
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
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (val state = articleState) {
                is ResultState.Loading -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                colors = CardDefaults.cardColors(containerColor = whiteColor)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )

                            }
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                colors = CardDefaults.cardColors(containerColor = whiteColor)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(15.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.6f)
                                            .height(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .shimmer()
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .shimmer()
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }
                            }
                        }
                    }
                }

                is ResultState.Error -> {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ResultState.Success -> {
                    val article = state.data
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 15.dp, vertical = 15.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(4.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = whiteColor)
                            ) {
                                AsyncImage(
                                    model = article.image ?: "",
                                    contentDescription = stringResource(R.string.article_image_desc),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                )
                            }
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(4.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = whiteColor)
                            ) {
                                Column(modifier = Modifier.padding(15.dp)) {
                                    Text(
                                        text = article.title ?: stringResource(R.string.no_title),
                                        fontSize = 12.sp,
                                        fontFamily = PoppinsSemiBold,
                                        color = textColor
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = article.content?.replace("\\n", "\n")
                                            ?: stringResource(R.string.no_content),
                                        fontSize = 12.sp,
                                        fontFamily = PoppinsMedium,
                                        color = textColor
                                    )
                                }
                            }
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                SSJetPackComposeProgressButton(
                                    type = SSButtonType.CIRCLE,
                                    width = 380.dp,
                                    height = 50.dp,
                                    buttonState = linkButtonState,
                                    onClick = {
                                        coroutineScope.launch {
                                            linkButtonState = SSButtonState.LOADING
                                            delay(1000)
                                            linkButtonState = SSButtonState.SUCCESS

                                            val rawUrl = article.urlArticle ?: return@launch

                                            val fullUrl = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
                                                rawUrl
                                            } else {
                                                "http://$rawUrl"
                                            }

                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                                            context.startActivity(intent)
                                        }
                                    },
                                    cornerRadius = 100,
                                    assetColor = Color.White,
                                    successIconPainter = null,
                                    failureIconPainter = null,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryColor,
                                        contentColor = whiteColor,
                                        disabledContainerColor = primaryColor,
                                    ),
                                    text = stringResource(R.string.link_article),
                                    textModifier = Modifier,
                                    fontSize = 14.sp,
                                    fontFamily = PoppinsSemiBold
                                )
                            }
                        }
                    }
                }

                else -> {
                    // No state to display
                }
            }
        }
    }
}