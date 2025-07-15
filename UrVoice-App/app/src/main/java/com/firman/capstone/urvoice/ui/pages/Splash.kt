package com.firman.capstone.urvoice.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.*
import com.firman.capstone.urvoice.R
import com.firman.capstone.urvoice.ui.navigation.Screen
import com.firman.capstone.urvoice.ui.theme.PoppinsRegular
import com.firman.capstone.urvoice.ui.viewmodel.SplashViewModel

@Composable
fun SplashScreen(navController: NavController, viewModel: SplashViewModel = hiltViewModel()) {
    val isInPreview = LocalInspectionMode.current
    val appStartupState by viewModel.appStartupState.collectAsState()

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.splashanimation)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    LaunchedEffect(progress, appStartupState) {
        if (progress >= 1f && !appStartupState.isLoading) {
            when {
                !appStartupState.isOnboardingCompleted -> {
                    navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
                appStartupState.isLoggedIn -> {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
                else -> {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (!isInPreview && composition != null) {
            LottieAnimation(
                composition = composition,
                progress = progress,
                modifier = Modifier.size(202.dp)
            )
        }

        Text(
            text = "UrVoice",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            color = Color.Black,
            fontSize = 16.sp,
            fontFamily = PoppinsRegular
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(navController = rememberNavController())
}