package com.firman.capstone.urvoice.ui.pages

import android.os.*
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.firman.capstone.urvoice.R
import com.firman.capstone.urvoice.ui.components.CustomAlertDialog
import com.firman.capstone.urvoice.ui.navigation.Screen
import com.firman.capstone.urvoice.ui.theme.PoppinsSemiBold
import com.firman.capstone.urvoice.ui.theme.UrVoiceTheme
import com.firman.capstone.urvoice.ui.theme.greyTextColor
import com.firman.capstone.urvoice.ui.theme.primaryColor
import com.firman.capstone.urvoice.ui.theme.textColor
import com.firman.capstone.urvoice.ui.theme.whiteColor
import com.firman.capstone.urvoice.ui.viewmodel.LoginViewModel
import com.firman.capstone.urvoice.utils.ResultState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonType
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSJetPackComposeProgressButton
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

@Composable
fun LoginItem() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.login_title),
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .padding(top = 193.dp),
            style = TextStyle(
                fontFamily = PoppinsSemiBold,
                fontSize = 26.sp,
                color = primaryColor,
                textAlign = TextAlign.Center,
            )
        )
        Text(
            text = stringResource(R.string.login_description),
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 16.dp),
            style = TextStyle(
                fontFamily = PoppinsSemiBold,
                fontSize = 20.sp,
                color = textColor,
                textAlign = TextAlign.Center,
            )
        )
    }
}

@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var loginButtonState by remember { mutableStateOf(SSButtonState.IDLE) }
    var loginAttempted by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var failedLoginDialog by remember { mutableStateOf(false) }

    val emailSubject = remember { BehaviorSubject.createDefault("") }
    val passwordSubject = remember { BehaviorSubject.createDefault("") }
    val compositeDisposable = remember { CompositeDisposable() }
    val loginState by viewModel.loginState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.resetLoginState()
    }

    LaunchedEffect(loginState) {
        when (loginState) {
            is ResultState.Initial -> {
                loginButtonState = SSButtonState.IDLE
            }

            is ResultState.Loading -> {
                loginButtonState = SSButtonState.LOADING
            }

            is ResultState.Success -> {
                loginButtonState = SSButtonState.SUCCESS
                val loginData = (loginState as ResultState.Success).data.data
                val token = loginData?.refreshToken
                if (!token.isNullOrEmpty()) {
                    viewModel.saveUserCredentials(email, password, token)
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.token_save_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }, 2000)
            }

            is ResultState.Error -> {
                loginButtonState = SSButtonState.FAILURE
                val errorMessage = (loginState as ResultState.Error).errorMessage
                if (errorMessage.contains("500")) {
                    failedLoginDialog = true
                } else {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    loginButtonState = SSButtonState.IDLE
                }, 500)
            }
        }
    }

    DisposableEffect(Unit) {
        val emailObs = emailSubject
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (loginAttempted) {
                    emailError = when {
                        it.isEmpty() -> context.getString(R.string.email_empty_error)
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(it)
                            .matches() -> context.getString(R.string.email_invalid_error)

                        else -> null
                    }
                }
            }

        val passObs = passwordSubject
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (loginAttempted) {
                    passwordError = when {
                        it.isEmpty() -> context.getString(R.string.password_empty_error)
                        it.length < 8 -> context.getString(R.string.password_min_length_error)
                        else -> null
                    }
                }
            }

        compositeDisposable.addAll(emailObs, passObs)

        onDispose { compositeDisposable.clear() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LoginItem()

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailSubject.onNext(it)
                },
                label = { Text(stringResource(R.string.email_label)) },
                isError = emailError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE3F2FD),
                    unfocusedContainerColor = Color(0xFFE3F2FD),
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Transparent
                ),
                textStyle = TextStyle(
                    color = if (email.isNotEmpty()) textColor else greyTextColor
                )
            )

            if (emailError != null) {
                Text(
                    text = emailError!!,
                    color = Color.Red,
                    style = TextStyle(fontSize = 12.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 8.dp)
                )
            }

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordSubject.onNext(it)
                },
                label = { Text(stringResource(R.string.password_label)) },
                isError = passwordError != null,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE3F2FD),
                    unfocusedContainerColor = Color(0xFFE3F2FD),
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Transparent
                ),
                textStyle = TextStyle(
                    color = if (password.isNotEmpty()) textColor else greyTextColor
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        if (passwordVisible) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                painterResource(id = R.drawable.ic_eye_primary),
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            )

            if (passwordError != null) {
                Text(
                    text = passwordError!!,
                    color = Color.Red,
                    style = TextStyle(fontSize = 12.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 8.dp)
                )
            }

            SSJetPackComposeProgressButton(
                type = SSButtonType.CIRCLE,
                width = 382.dp,
                height = 50.dp,
                cornerRadius = 10,
                assetColor = whiteColor,
                text = stringResource(R.string.login_button),
                textModifier = Modifier.padding(horizontal = 15.dp),
                fontSize = 16.sp,
                fontFamily = PoppinsSemiBold,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = whiteColor,
                    disabledContainerColor = primaryColor.copy(alpha = 0.6f)
                ),
                buttonState = loginButtonState,
                onClick = {
                    loginAttempted = true
                    val isEmailValid = when {
                        email.isEmpty() -> {
                            emailError = context.getString(R.string.email_empty_error); false
                        }

                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                            emailError = context.getString(R.string.email_invalid_error); false
                        }

                        else -> {
                            emailError = null; true
                        }
                    }
                    val isPasswordValid = when {
                        password.isEmpty() -> {
                            passwordError = context.getString(R.string.password_empty_error); false
                        }

                        password.length < 8 -> {
                            passwordError =
                                context.getString(R.string.password_min_length_error); false
                        }

                        else -> {
                            passwordError = null; true
                        }
                    }

                    if (isEmailValid && isPasswordValid) {
                        viewModel.login(email, password)
                    }
                }
            )

            TextButton(
                onClick = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .padding(top = 24.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = stringResource(R.string.register_link),
                    color = textColor,
                    fontFamily = PoppinsSemiBold,
                    style = TextStyle(fontSize = 14.sp)
                )
            }
        }

        if (failedLoginDialog) {
            Dialog(onDismissRequest = { failedLoginDialog = false }) {
                CustomAlertDialog(
                    title = stringResource(R.string.email_not_registered_title),
                    message = stringResource(R.string.email_not_registered_message),
                    positiveText = stringResource(R.string.register_now),
                    negativeText = stringResource(R.string.cancel),
                    onConfirm = {
                        failedLoginDialog = false
                        navController.navigate(Screen.Register.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onDismiss = {
                        failedLoginDialog = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    UrVoiceTheme {
        LoginScreen(navController = NavController(LocalContext.current))
    }
}