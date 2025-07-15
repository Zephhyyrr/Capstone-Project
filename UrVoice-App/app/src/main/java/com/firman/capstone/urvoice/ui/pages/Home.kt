package com.firman.capstone.urvoice.ui.pages

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.airbnb.lottie.compose.*
import com.firman.capstone.urvoice.R
import com.firman.capstone.urvoice.data.remote.models.ArticleResponse
import com.firman.capstone.urvoice.data.remote.models.CurrentUserResponse
import com.firman.capstone.urvoice.data.remote.models.HistoryResponse
import com.firman.capstone.urvoice.ui.components.CardHomeArticle
import com.firman.capstone.urvoice.ui.components.HistoryCard
import com.firman.capstone.urvoice.ui.theme.PoppinsSemiBold
import com.firman.capstone.urvoice.ui.theme.primaryColor
import com.firman.capstone.urvoice.ui.theme.textColor
import com.firman.capstone.urvoice.ui.theme.whiteBackground
import com.firman.capstone.urvoice.ui.theme.whiteColor
import com.firman.capstone.urvoice.ui.viewmodel.HomeViewModel
import com.firman.capstone.urvoice.utils.FormatDateUtils
import com.firman.capstone.urvoice.utils.MediaUrlUtils
import com.firman.capstone.urvoice.utils.QueryHistoryUtils
import com.firman.capstone.urvoice.utils.ResultState

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val articlesState by viewModel.articlesState.collectAsStateWithLifecycle()
    val historyState by viewModel.historyState.collectAsStateWithLifecycle()
    val isLoading = articlesState is ResultState.Loading || historyState is ResultState.Loading
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(primaryColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 16.dp)
            ) {
                if (userState is ResultState.Success) {
                    val user = (userState as ResultState.Success<CurrentUserResponse>).data
                    UserHeader(user)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-40).dp)
                .background(
                    color = whiteBackground,
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                )
                .padding(top = 40.dp, start = 15.dp, end = 15.dp, bottom = 90.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 100.dp, bottom = 100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_animation))
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(150.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionTitle(stringResource(R.string.article_home_title))
                    ArticlesContent(articlesState, navController, viewModel)
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionTitle(stringResource(R.string.update_history_title_home))

                    if (historyState is ResultState.Success) {
                        val historyList =
                            (historyState as ResultState.Success<List<HistoryResponse.Data>>).data
                        val latestHistory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            QueryHistoryUtils.getLatestHistory(historyList)
                        } else null

                        latestHistory?.let { historyItem ->
                            val (dayMonth, year) = FormatDateUtils.formatDate(historyItem.createdAt)
                            HistoryCard(
                                audioFileName = historyItem.fileAudio.orEmpty(),
                                correctedParagraph = historyItem.correctedParagraph.orEmpty(),
                                dayMonth = dayMonth,
                                year = year,
                                onClick = {
                                    navController.navigate("history_detail/${historyItem.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserHeader(user: CurrentUserResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        ProfileIcon(user.data?.profileImage?.toString() ?: "")
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            modifier = Modifier
                .padding(end = 8.dp),
            text = "Hello, ${user.data?.name ?: "User"}",
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = PoppinsSemiBold,
                fontWeight = FontWeight.SemiBold,
                color = whiteColor,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProfileIcon(
    imageUrl: String?,
) {
    val context = LocalContext.current
    val finalUrl = MediaUrlUtils.buildMediaUrl(imageUrl)

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(finalUrl.ifBlank { null })
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.unknownperson),
            error = painterResource(R.drawable.unknownperson),
            contentDescription = "Profile",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
        )
    }
}


@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = TextStyle(
            fontSize = 16.sp,
            fontFamily = PoppinsSemiBold,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        ),
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
private fun ArticlesContent(
    articlesState: ResultState<*>,
    navController: NavController,
    viewModel: HomeViewModel
) {
    when (articlesState) {
        is ResultState.Success -> {
            val articles =
                (articlesState as ResultState.Success<List<ArticleResponse.Data>>).data.take(5)

            if (articles.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(articles) { article ->
                        CardHomeArticle(
                            imageUrl = article.image.orEmpty(),
                            onClick = { navController.navigate("article/${article.id}") }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.nodata_animation))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(250.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = stringResource(R.string.nodata_article_title),
                            style = TextStyle(
                                color = textColor,
                                fontSize = 14.sp,
                                fontFamily = PoppinsSemiBold
                            )
                        )
                    }
                }
            }
        }

        is ResultState.Error -> {
            ArticlesListError(
                onRetry = viewModel::getAllArticles,
                error = articlesState.toString()
            )
        }

        else -> {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(3) { ArticleCardSkeleton() }
            }
        }
    }
}

@Composable
private fun ArticleCardSkeleton() {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.size(width = 327.dp, height = 180.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray.copy(alpha = 0.3f))
        )
    }
}

@Composable
private fun ArticlesListError(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Failed to load articles",
            style = TextStyle(fontSize = 14.sp, color = Color.Gray)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("Retry", color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController = navController)
}
