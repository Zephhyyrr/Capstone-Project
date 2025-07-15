package com.firman.capstone.urvoice.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.firman.capstone.urvoice.R
import com.firman.capstone.urvoice.data.remote.models.ArticleResponse
import com.firman.capstone.urvoice.ui.components.CardArticle
import com.firman.capstone.urvoice.ui.theme.PoppinsSemiBold
import com.firman.capstone.urvoice.ui.theme.textColor
import com.firman.capstone.urvoice.ui.theme.whiteColor
import com.firman.capstone.urvoice.utils.ResultState
import com.firman.capstone.urvoice.ui.viewmodel.ArticleViewModel
import com.firman.capstone.urvoice.ui.navigation.Screen
import com.firman.capstone.urvoice.ui.theme.primaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    modifier: Modifier = Modifier,
    viewModel: ArticleViewModel,
    navController: NavController
) {
    val articleState by viewModel.articles.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.menu_article_title),
                        fontSize = 14.sp,
                        fontFamily = PoppinsSemiBold,
                        color = whiteColor
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = primaryColor
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (articleState) {
                is ResultState.Loading -> {
                    LottieAnimation(
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.Center),
                        composition = rememberLottieComposition(
                            LottieCompositionSpec.RawRes(R.raw.loading_animation)
                        ).value,
                        iterations = LottieConstants.IterateForever,
                        contentScale = ContentScale.Fit
                    )
                }

                is ResultState.Success -> {
                    val articles =
                        (articleState as ResultState.Success<List<ArticleResponse.Data>>).data
                    if (articles.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(15.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                LottieAnimation(
                                    modifier = Modifier.size(400.dp),
                                    composition = rememberLottieComposition(
                                        LottieCompositionSpec.RawRes(R.raw.nodata_animation)
                                    ).value,
                                    iterations = LottieConstants.IterateForever,
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    text = stringResource(R.string.nodata_article_title),
                                    style = TextStyle(
                                        color = textColor,
                                        fontSize = 16.sp,
                                        fontFamily = PoppinsSemiBold
                                    )
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .padding(bottom = 80.dp),
                            contentPadding = PaddingValues(15.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(articles) { article ->
                                CardArticle(
                                    title = article.title ?: "",
                                    content = article.content ?: "",
                                    imageUrl = article.image ?: "",
                                    onClick = {
                                        navController.navigate(
                                            Screen.Article(article.id.toString()).articleRoute()
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                is ResultState.Error -> {
                    val errorMessage = (articleState as ResultState.Error).errorMessage
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ResultState.Initial -> {
                    // Initial State
                }
            }
        }
    }
}
