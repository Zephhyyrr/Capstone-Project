package com.firman.capstone.urvoice.ui.pages

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.firman.capstone.urvoice.R
import com.firman.capstone.urvoice.ui.components.CustomAlertDialog
import com.firman.capstone.urvoice.ui.theme.*
import com.firman.capstone.urvoice.ui.viewmodel.ProfileViewModel
import com.firman.capstone.urvoice.utils.GalleryUtils
import com.firman.capstone.urvoice.utils.ResultState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageProfilePreviewScreen(
    imageUri: Uri,
    onBackClick: () -> Unit = {},
    onImageUploaded: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var uploadButtonState by remember { mutableStateOf(SSButtonState.IDLE) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val uploadState by viewModel.uploadProfileImageState.collectAsState()

    LaunchedEffect(uploadState) {
        when (uploadState) {
            is ResultState.Success -> {
                uploadButtonState = SSButtonState.SUCCESS
                isProcessing = false
                showErrorDialog = false
                errorMessage = ""
                showSuccessDialog = true
            }

            is ResultState.Error -> {
                uploadButtonState = SSButtonState.FAILURE
                isProcessing = false
                errorMessage = (uploadState as ResultState.Error).errorMessage
                showErrorDialog = true
            }

            is ResultState.Loading -> {
                uploadButtonState = SSButtonState.LOADING
                isProcessing = true
            }

            else -> {
                if (!isProcessing) {
                    uploadButtonState = SSButtonState.IDLE
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetUploadState()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.preview_image),
                        fontSize = 16.sp,
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Image Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = stringResource(R.string.profile_preview_image),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.unknownperson),
                    error = painterResource(id = R.drawable.unknownperson)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.image_preview_description),
                fontSize = 14.sp,
                fontFamily = PoppinsRegular,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            SSJetPackComposeProgressButton(
                type = SSButtonType.CIRCLE,
                width = 380.dp,
                height = 50.dp,
                buttonState = uploadButtonState,
                onClick = {
                    if (uploadButtonState == SSButtonState.IDLE || uploadButtonState == SSButtonState.FAILURE) {
                        coroutineScope.launch {
                            try {
                                isProcessing = true
                                uploadButtonState = SSButtonState.LOADING

                                if (!GalleryUtils.isImageFile(imageUri, context)) {
                                    errorMessage = context.getString(R.string.invalid_image_format)
                                    showErrorDialog = true
                                    uploadButtonState = SSButtonState.FAILURE
                                    isProcessing = false
                                    return@launch
                                }

                                val imageFile = GalleryUtils.processImageFromUri(
                                    context = context,
                                    uri = imageUri,
                                    maxWidth = 1024,
                                    maxHeight = 1024,
                                    quality = 80
                                )

                                if (imageFile != null) {
                                    val fileSizeInMB = GalleryUtils.getFileSizeInMB(imageFile)
                                    if (fileSizeInMB > 5.0) { // 5MB limit
                                        errorMessage = context.getString(R.string.image_too_large)
                                        showErrorDialog = true
                                        uploadButtonState = SSButtonState.FAILURE
                                        isProcessing = false
                                        return@launch
                                    }

                                    viewModel.uploadProfileImage(imageFile)
                                } else {
                                    errorMessage =
                                        context.getString(R.string.image_processing_error)
                                    showErrorDialog = true
                                    uploadButtonState = SSButtonState.FAILURE
                                    isProcessing = false
                                }
                            } catch (e: Exception) {
                                errorMessage =
                                    e.message ?: context.getString(R.string.upload_failed)
                                showErrorDialog = true
                                uploadButtonState = SSButtonState.FAILURE
                                isProcessing = false
                            }
                        }
                    }
                },
                cornerRadius = 25,
                assetColor = whiteColor,
                successIconPainter = null,
                failureIconPainter = null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = whiteColor,
                    disabledContainerColor = primaryColor.copy(alpha = 0.6f)
                ),
                text = when (uploadButtonState) {
                    SSButtonState.LOADING -> stringResource(R.string.uploading)
                    SSButtonState.SUCCESS -> stringResource(R.string.upload_success)
                    SSButtonState.FAILURE -> stringResource(R.string.retry_upload)
                    else -> stringResource(R.string.upload_image)
                },
                textModifier = Modifier,
                fontSize = 14.sp,
                fontFamily = PoppinsSemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showSuccessDialog) {
        Dialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.resetUploadState()
                onImageUploaded()
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            CustomAlertDialog(
                title = stringResource(R.string.success),
                message = stringResource(R.string.upload_image_success),
                positiveText = stringResource(R.string.ok),
                negativeText = "",
                onConfirm = {
                    showSuccessDialog = false
                    viewModel.resetUploadState()
                    onImageUploaded()
                },
                onDismiss = {
                    showSuccessDialog = false
                    viewModel.resetUploadState()
                    onImageUploaded()
                }
            )
        }
    }

    if (showErrorDialog) {
        Dialog(
            onDismissRequest = {
                showErrorDialog = false
                uploadButtonState = SSButtonState.IDLE
                viewModel.resetUploadState()
                isProcessing = false
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            CustomAlertDialog(
                title = stringResource(R.string.error),
                message = errorMessage,
                positiveText = stringResource(R.string.ok),
                negativeText = "",
                onConfirm = {
                    showErrorDialog = false
                    uploadButtonState = SSButtonState.IDLE
                    viewModel.resetUploadState()
                    isProcessing = false
                },
                onDismiss = {
                    showErrorDialog = false
                    uploadButtonState = SSButtonState.IDLE
                    viewModel.resetUploadState()
                    isProcessing = false
                }
            )
        }
    }
}