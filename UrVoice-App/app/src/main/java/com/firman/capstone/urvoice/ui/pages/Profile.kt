package com.firman.capstone.urvoice.ui.pages

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.firman.capstone.urvoice.R
import com.firman.capstone.urvoice.ui.components.CustomAlertDialog
import com.firman.capstone.urvoice.ui.components.ProfileCardContent
import com.firman.capstone.urvoice.ui.navigation.Screen
import com.firman.capstone.urvoice.ui.theme.*
import com.firman.capstone.urvoice.ui.viewmodel.ProfileViewModel
import com.firman.capstone.urvoice.utils.MediaUrlUtils
import com.firman.capstone.urvoice.utils.ResultState
import android.provider.Settings
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonType
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSJetPackComposeProgressButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel,
    navController: NavHostController,
    onEditProfileClick: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var signOutButtonState by remember { mutableStateOf(SSButtonState.IDLE) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.menu_profile_title),
                        fontSize = 14.sp,
                        fontFamily = PoppinsSemiBold,
                        color = whiteColor
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            ProfileCard(
                viewModel = viewModel,
                onEditClick = onEditProfileClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.setting_profile_title),
                fontSize = 16.sp,
                fontFamily = PoppinsSemiBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            SettingsItem(
                icon = painterResource(R.drawable.ic_language),
                title = stringResource(R.string.language),
                onClick = {
                    val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsItem(
                icon = painterResource(R.drawable.ic_delete),
                title = stringResource(R.string.delete_account),
                onClick = {
                    showDeleteDialog = true
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SSJetPackComposeProgressButton(
                    type = SSButtonType.CIRCLE,
                    width = 380.dp,
                    height = 50.dp,
                    buttonState = signOutButtonState,
                    onClick = {
                        showSignOutDialog = true
                    },
                    cornerRadius = 100,
                    assetColor = whiteColor,
                    successIconPainter = null,
                    failureIconPainter = null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = statusRedColor,
                        contentColor = whiteColor,
                        disabledContainerColor = statusRedColor
                    ),
                    text = stringResource(R.string.sign_out),
                    textModifier = Modifier,
                    fontSize = 14.sp,
                    fontFamily = PoppinsSemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showSignOutDialog) {
        Dialog(onDismissRequest = { showSignOutDialog = false }) {
            CustomAlertDialog(
                title = stringResource(R.string.sign_out_confirmation),
                message = stringResource(R.string.sign_out_message),
                positiveText = stringResource(R.string.yes),
                negativeText = stringResource(R.string.no),
                onConfirm = {
                    showSignOutDialog = false
                    coroutineScope.launch {
                        signOutButtonState = SSButtonState.LOADING
                        viewModel.logout()

                        viewModel.logoutUserState.collect { result ->
                            when (result) {
                                is ResultState.Success -> {
                                    signOutButtonState = SSButtonState.SUCCESS
                                    navController.navigate(Screen.OnBoarding.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                    return@collect
                                }

                                is ResultState.Error -> {
                                    signOutButtonState = SSButtonState.FAILURE
                                    return@collect
                                }

                                else -> Unit
                            }
                        }
                    }
                },
                onDismiss = {
                    showSignOutDialog = false
                }
            )
        }
    }

    if (showDeleteDialog) {
        Dialog(onDismissRequest = { showDeleteDialog = false }) {
            CustomAlertDialog(
                title = stringResource(R.string.delete_account),
                message = stringResource(R.string.delete_account_message),
                positiveText = stringResource(R.string.yes),
                negativeText = stringResource(R.string.no),
                onConfirm = {
                    showDeleteDialog = false
                    coroutineScope.launch {
                        viewModel.deleteUser()
                        navController.navigate(Screen.OnBoarding.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                },
                onDismiss = {
                    showDeleteDialog = false
                }
            )
        }
    }
}

@Composable
private fun ProfileCard(
    viewModel: ProfileViewModel,
    onEditClick: () -> Unit
) {
    val userProfileState = viewModel.currentUserProfile.collectAsState()

    when (val state = userProfileState.value) {
        is ResultState.Loading -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val composition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(R.raw.loading_animation)
                    )

                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }

        is ResultState.Success -> {
            val imageUrl = state.data.data?.profileImage?.let {
                MediaUrlUtils.buildMediaUrl(it.toString())
            } ?: ""

            ProfileCardContent(
                name = state.data.data?.name ?: stringResource(R.string.unknown_name),
                email = state.data.data?.email ?: stringResource(R.string.no_email_provided),
                profileImage = imageUrl,
                onEditClick = onEditClick
            )
        }

        is ResultState.Error -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }
            }
        }

        else -> Unit
    }
}

@Composable
private fun SettingsItem(
    icon: Painter,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = title,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                color = textColor,
                fontFamily = PoppinsMedium,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.navigate_forward),
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}