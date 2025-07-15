package com.firman.capstone.urvoice.ui.pages

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.firman.capstone.urvoice.R
import com.firman.capstone.urvoice.data.remote.models.CurrentUserResponse
import com.firman.capstone.urvoice.ui.components.CustomAlertDialog
import com.firman.capstone.urvoice.ui.theme.*
import com.firman.capstone.urvoice.ui.viewmodel.ProfileViewModel
import com.firman.capstone.urvoice.utils.GalleryUtils
import com.firman.capstone.urvoice.utils.MediaUrlUtils
import com.firman.capstone.urvoice.utils.ResultState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onNavigateToImagePreview: (Uri) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val currentUserState by viewModel.currentUserProfile.collectAsState()
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var formSubmitted by remember { mutableStateOf(false) }
    var saveButtonState by remember { mutableStateOf(SSButtonState.IDLE) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showImagePermissionDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { selectedUri ->

                try {
                    val isValidImage = GalleryUtils.isImageFile(selectedUri, context)
                    if (isValidImage) {
                        onNavigateToImagePreview(selectedUri)
                    } else {
                        showImagePermissionDialog = true
                    }
                } catch (e: Exception) {
                    showImagePermissionDialog = true
                }
            } ?: run {
                Log.d("EditProfileScreen", "No URI selected")
            }
        }
    )

    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { selectedUri ->
                onNavigateToImagePreview(selectedUri)
            }
        }
    )

    val imageUrl = (currentUserState as? ResultState.Success)?.data?.data?.profileImage?.let {
        MediaUrlUtils.buildMediaUrl(it.toString())
    } ?: ""

    LaunchedEffect(currentUserState) {
        if (currentUserState is ResultState.Success) {
            val userData = (currentUserState as ResultState.Success<CurrentUserResponse>).data.data
            userName = userData?.name ?: ""
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.edit_profile_title),
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
                },
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 15.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .border(width = 2.dp, color = primaryColor, shape = CircleShape)
                            .clickable { galleryLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = stringResource(R.string.profile_image_desc),
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.unknownperson),
                            error = painterResource(id = R.drawable.unknownperson)
                        )
                    }

                    // Edit Icon
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-4).dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                            .clickable { galleryLauncher.launch("image/*") }
                            .zIndex(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_profile_image),
                            tint = whiteColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (currentUserState) {
                    is ResultState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LottieAnimation(
                                modifier = Modifier.size(150.dp),
                                composition = rememberLottieComposition(
                                    LottieCompositionSpec.RawRes(
                                        R.raw.loading_animation
                                    )
                                ).value,
                                iterations = LottieConstants.IterateForever,
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    is ResultState.Error -> {
                        Log.d(
                            "EditProfileScreen",
                            "Error: ${(currentUserState as ResultState.Error).errorMessage}"
                        )
                    }

                    is ResultState.Success -> {
                        val userData =
                            (currentUserState as ResultState.Success<CurrentUserResponse>).data.data

                        Text(
                            text = stringResource(R.string.edit_email_title),
                            fontSize = 16.sp,
                            fontFamily = PoppinsSemiBold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = userData?.email ?: "",
                            onValueChange = {},
                            readOnly = true,
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.user_email),
                                    fontFamily = PoppinsRegular
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(whiteColor, RoundedCornerShape(12.dp)),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 14.sp,
                                fontFamily = PoppinsRegular,
                                color = textColor
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFE3F2FD),
                                unfocusedContainerColor = Color(0xFFE3F2FD),
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.edit_name_title),
                            fontSize = 16.sp,
                            fontFamily = PoppinsSemiBold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = userName,
                            onValueChange = {
                                userName = it
                                if (formSubmitted) {
                                    nameError = when {
                                        it.isEmpty() -> context.getString(R.string.name_empty_error)
                                        it.length < 3 -> context.getString(R.string.name_min_length_error)
                                        else -> null
                                    }
                                }
                            },
                            placeholder = {
                                Text(
                                    text = userData?.name
                                        ?: stringResource(R.string.enter_your_name),
                                    fontFamily = PoppinsRegular
                                )
                            },
                            isError = nameError != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(whiteColor, RoundedCornerShape(12.dp)),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 14.sp,
                                fontFamily = PoppinsRegular,
                                color = textColor
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFE3F2FD),
                                unfocusedContainerColor = Color(0xFFE3F2FD),
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        nameError?.let {
                            Text(
                                text = it,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.edit_password_title),
                            fontSize = 16.sp,
                            fontFamily = PoppinsSemiBold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                if (formSubmitted) passwordError =
                                    if (it.isNotEmpty() && it.length < 8) context.getString(R.string.password_min_length_error) else null
                            },
                            placeholder = { Text(text = "********", fontFamily = PoppinsRegular) },
                            isError = passwordError != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(whiteColor, RoundedCornerShape(12.dp)),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 14.sp,
                                fontFamily = PoppinsRegular,
                                color = textColor
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFE3F2FD),
                                unfocusedContainerColor = Color(0xFFE3F2FD),
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    if (passwordVisible) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = stringResource(R.string.hide_password),
                                            tint = primaryColor
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_eye_primary),
                                            contentDescription = stringResource(R.string.show_password),
                                            tint = primaryColor
                                        )
                                    }
                                }
                            }
                        )
                        passwordError?.let {
                            Text(
                                text = it,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }

                    is ResultState.Initial -> {}
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                SSJetPackComposeProgressButton(
                    type = SSButtonType.CIRCLE,
                    width = 380.dp,
                    height = 50.dp,
                    buttonState = saveButtonState,
                    onClick = {
                        formSubmitted = true
                        val isNameValid = if (userName.isEmpty()) {
                            nameError = context.getString(R.string.name_empty_error)
                            false
                        } else {
                            nameError = null
                            true
                        }
                        val isPasswordValid = if (password.isNotEmpty() && password.length < 8) {
                            passwordError = context.getString(R.string.password_min_length_error)
                            false
                        } else {
                            passwordError = null
                            true
                        }
                        if (isNameValid && isPasswordValid) {
                            coroutineScope.launch {
                                saveButtonState = SSButtonState.LOADING
                                viewModel.updateUserProfile(
                                    userName,
                                    if (password.isBlank()) null else password
                                )
                            }
                            saveButtonState = SSButtonState.SUCCESS
                            showSuccessDialog = true
                        } else {
                            saveButtonState = SSButtonState.FAILURE
                        }
                    },
                    cornerRadius = 100,
                    assetColor = whiteColor,
                    successIconPainter = null,
                    failureIconPainter = null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = whiteColor,
                        disabledContainerColor = primaryColor
                    ),
                    text = stringResource(R.string.save),
                    textModifier = Modifier,
                    fontSize = 14.sp,
                    fontFamily = PoppinsSemiBold
                )
            }
        }
    }

    if (showSuccessDialog) {
        Dialog(onDismissRequest = { showSuccessDialog = false }) {
            CustomAlertDialog(
                title = stringResource(R.string.success),
                message = stringResource(R.string.save_profile_success),
                positiveText = stringResource(R.string.ok),
                onConfirm = {
                    showSuccessDialog = false
                    onSaveClick()
                },
                onDismiss = {
                    showSuccessDialog = false
                }
            )
        }
    }

    if (showImagePermissionDialog) {
        CustomAlertDialog(
            title = stringResource(R.string.error),
            message = stringResource(R.string.invalid_image_file),
            positiveText = stringResource(R.string.ok),
            onConfirm = {
                showImagePermissionDialog = false
            },
            onDismiss = {
                showImagePermissionDialog = false
            }
        )
    }
}