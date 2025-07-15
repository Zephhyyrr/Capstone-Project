package com.firman.capstone.urvoice

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.livedata.observeAsState
import com.firman.capstone.urvoice.ui.navigation.BottomAppBarWithFab
import com.firman.capstone.urvoice.ui.navigation.BottomNavItem
import com.firman.capstone.urvoice.ui.navigation.Screen
import com.firman.capstone.urvoice.ui.pages.AnalyzeScreen
import com.firman.capstone.urvoice.ui.pages.ArticleDetailScreen
import com.firman.capstone.urvoice.ui.pages.ArticleScreen
import com.firman.capstone.urvoice.ui.pages.EditProfileScreen
import com.firman.capstone.urvoice.ui.pages.HistoryDetailScreen
import com.firman.capstone.urvoice.ui.pages.HistoryScreen
import com.firman.capstone.urvoice.ui.pages.HomeScreen
import com.firman.capstone.urvoice.ui.pages.ImageProfilePreviewScreen
import com.firman.capstone.urvoice.ui.pages.LoginScreen
import com.firman.capstone.urvoice.ui.pages.OnBoardingScreen
import com.firman.capstone.urvoice.ui.pages.ProfileScreen
import com.firman.capstone.urvoice.ui.pages.RegisterScreen
import com.firman.capstone.urvoice.ui.pages.SignScreen
import com.firman.capstone.urvoice.ui.pages.RecordScreen
import com.firman.capstone.urvoice.ui.pages.SpeechToTextScreen
import com.firman.capstone.urvoice.ui.pages.SplashScreen
import com.firman.capstone.urvoice.ui.theme.UrVoiceTheme
import com.firman.capstone.urvoice.ui.viewmodel.ArticleViewModel
import com.firman.capstone.urvoice.ui.viewmodel.ProfileViewModel
import com.firman.capstone.urvoice.ui.viewmodel.SpeechViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.core.net.toUri
import com.firman.capstone.urvoice.ui.viewmodel.HistoryViewModel
import com.firman.capstone.urvoice.ui.viewmodel.HomeViewModel

@Composable
fun UrVoiceRootApp(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val routesWithBottomNav = listOf(
        Screen.Home.route,
        "article",
        Screen.Record.route,
        Screen.History.route,
        Screen.Profile.route
    )

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home.route, "Home", painterResource(R.drawable.ic_home)),
        BottomNavItem("article", "Article", painterResource(R.drawable.ic_articles)),
        BottomNavItem(
            Screen.Record.route,
            "Voice",
            painterResource(R.drawable.ic_mic),
            isMainFeature = true
        ),
        BottomNavItem(Screen.History.route, "History", painterResource(R.drawable.ic_history)),
        BottomNavItem(Screen.Profile.route, "Profile", painterResource(R.drawable.ic_profile))
    )

    val sharedSpeechViewModel: SpeechViewModel = hiltViewModel()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(navController)
            }
            composable(Screen.OnBoarding.route) {
                OnBoardingScreen(
                    navController = navController,
                    onFinishOnboarding = {
                        navController.navigate(Screen.Sign.route) {
                            popUpTo(Screen.OnBoarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Sign.route) {
                SignScreen(navController)
            }
            composable(Screen.Login.route) {
                LoginScreen(navController)
            }
            composable(Screen.Register.route) {
                RegisterScreen(navController)
            }
            composable(Screen.Home.route) { backStackEntry ->
                val viewModel: HomeViewModel = hiltViewModel()
                val homeEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.Home.route)
                }

                val refreshHome by homeEntry
                    .savedStateHandle
                    .getLiveData<Boolean>("refreshHome")
                    .observeAsState()

                LaunchedEffect(refreshHome) {
                    if (refreshHome == true) {
                        viewModel.loadInitialData()
                        homeEntry.savedStateHandle["refreshHome"] = false
                    }
                }

                HomeScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }

            composable("article") {
                val viewModel: ArticleViewModel = hiltViewModel()
                ArticleScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable("article/{id}") { backStackEntry ->
                val viewModel: ArticleViewModel = hiltViewModel()
                val articleId =
                    backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable

                ArticleDetailScreen(
                    viewModel = viewModel,
                    articleId = articleId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Record.route) {
                RecordScreen(
                    viewModel = sharedSpeechViewModel,
                    onNavigateToSpeechToText = {
                        navController.navigate(Screen.SpeechToText.route)
                    }
                )
            }

            composable(Screen.SpeechToText.route) {
                SpeechToTextScreen(
                    viewModel = sharedSpeechViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNavigateRecordScreen = {
                        navController.navigate(Screen.Record.route) {
                            popUpTo(Screen.SpeechToText.route) { inclusive = true }
                        }
                    },
                    onNavigateAnalyzeScreen = { text, fileName ->
                        val encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
                        val encodedAudio =
                            URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())

                        navController.navigate("analyze/$encodedText/$encodedAudio") {
                            popUpTo(Screen.SpeechToText.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "analyze/{text}/{audioFileName}",
                arguments = listOf(
                    navArgument("text") { type = NavType.StringType },
                    navArgument("audioFileName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val encodedText = backStackEntry.arguments?.getString("text") ?: ""
                val encodedAudio = backStackEntry.arguments?.getString("audioFileName") ?: ""

                val text = URLDecoder.decode(encodedText, StandardCharsets.UTF_8.toString())
                val audioFileName =
                    URLDecoder.decode(encodedAudio, StandardCharsets.UTF_8.toString())

                AnalyzeScreen(
                    text = text,
                    audioFileName = audioFileName,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }

                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("refreshHistory", true)

                        navController.getBackStackEntry(Screen.Home.route)
                            .savedStateHandle["refreshHome"] = true
                    }

                )
            }

            composable(Screen.History.route) {
                val viewModel: HistoryViewModel = hiltViewModel()

                val currentEntry by navController.currentBackStackEntryAsState()
                val refreshTrigger = currentEntry
                    ?.savedStateHandle
                    ?.getLiveData<Boolean>("refreshHistory")
                    ?.observeAsState()

                LaunchedEffect(refreshTrigger?.value) {
                    if (refreshTrigger?.value == true) {
                        viewModel.getAllHistory()
                        currentEntry?.savedStateHandle?.set("refreshHistory", false)
                    }
                }

                HistoryScreen(
                    viewModel = viewModel,
                    onHistoryItemClick = { historyData ->
                        navController.navigate(Screen.HistoryDetail(historyData.id).createRoute())
                    }
                )
            }

            composable(
                route = "history_detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val historyId = backStackEntry.arguments?.getInt("id") ?: return@composable

                HistoryDetailScreen(
                    historyId = historyId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                val viewModel: ProfileViewModel = hiltViewModel()

                val shouldRefresh = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getLiveData<Boolean>("refreshProfile")
                    ?.observeAsState()

                LaunchedEffect(shouldRefresh?.value) {
                    if (shouldRefresh?.value == true) {
                        viewModel.getCurrentUser()
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("refreshProfile", false)
                    }
                }

                LaunchedEffect(Unit) {
                    viewModel.getCurrentUser()
                }

                ProfileScreen(
                    viewModel = viewModel,
                    navController = navController,
                    onEditProfileClick = {
                        navController.navigate(Screen.EditProfile.route)
                    }
                )
            }

            composable(Screen.EditProfile.route) {
                val viewModel: ProfileViewModel = hiltViewModel()

                EditProfileScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("refreshProfile", true)

                        val homeEntry = navController.getBackStackEntry(Screen.Home.route)
                        homeEntry.savedStateHandle["refreshHome"] = true

                        navController.popBackStack()
                    },
                    onNavigateToImagePreview = { imageUri ->
                        val encodedUri = URLEncoder.encode(
                            imageUri.toString(),
                            StandardCharsets.UTF_8.toString()
                        )
                        navController.navigate("image_profile_preview/$encodedUri")
                    }
                )
            }

            composable(
                route = "image_profile_preview/{imageUri}",
                arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
            ) { backStackEntry ->
                val viewModel: ProfileViewModel = hiltViewModel()
                val encodedUri =
                    backStackEntry.arguments?.getString("imageUri") ?: return@composable
                val imageUri =
                    URLDecoder.decode(encodedUri, StandardCharsets.UTF_8.toString()).toUri()

                ImageProfilePreviewScreen(
                    imageUri = imageUri,
                    onBackClick = { navController.popBackStack() },
                    onImageUploaded = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("refreshProfile", true)

                        val homeEntry = navController.getBackStackEntry(Screen.Home.route)
                        homeEntry.savedStateHandle["refreshHome"] = true

                        navController.popBackStack()
                    },
                    viewModel = viewModel
                )
            }
        }

        if (currentRoute in routesWithBottomNav) {
            BottomAppBarWithFab(
                items = bottomNavItems,
                navController = navController,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun UrVoiceApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BackHandler(enabled = true) {
        when (currentRoute) {
            Screen.Home.route,
            "article",
            Screen.Record.route,
            Screen.History.route,
            Screen.Profile.route,
            Screen.Login.route,
            Screen.Register.route,
            Screen.Sign.route,
            Screen.OnBoarding.route -> {
                (context as? Activity)?.finish()
            }

            else -> {
                if (!navController.popBackStack()) {
                    (context as? Activity)?.finish()
                }
            }
        }
    }
    UrVoiceRootApp(navController)
}

@Preview(showBackground = true)
@Composable
fun UrVoiceAppPreview() {
    UrVoiceTheme {
        UrVoiceRootApp()
    }
}