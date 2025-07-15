package com.firman.capstone.urvoice.ui.pages

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.navigation.compose.rememberNavController
import com.firman.capstone.urvoice.R
import com.firman.capstone.urvoice.ui.components.CustomAlertDialog
import com.firman.capstone.urvoice.ui.navigation.Screen
import com.firman.capstone.urvoice.ui.theme.PoppinsSemiBold
import com.firman.capstone.urvoice.ui.theme.UrVoiceTheme
import com.firman.capstone.urvoice.ui.theme.greyTextColor
import com.firman.capstone.urvoice.ui.theme.primaryColor
import com.firman.capstone.urvoice.ui.theme.textColor
import com.firman.capstone.urvoice.ui.theme.whiteColor
import com.firman.capstone.urvoice.ui.viewmodel.RegisterViewModel
import com.firman.capstone.urvoice.utils.ResultState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonType
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSJetPackComposeProgressButton
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

@Composable
fun RegisterItem() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.register_title),
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .padding(top = 141.dp),
            style = TextStyle(
                fontFamily = PoppinsSemiBold,
                fontSize = 26.sp,
                color = primaryColor,
                textAlign = TextAlign.Center
            )
        )
        Text(
            text = stringResource(R.string.register_description),
            modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 16.dp)
                .padding(top = 8.dp),
            style = TextStyle(
                fontFamily = PoppinsSemiBold,
                fontSize = 16.sp,
                color = primaryColor,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun RegisterScreen(navController: NavController, viewModel: RegisterViewModel = hiltViewModel()) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var registerButtonState by remember { mutableStateOf(SSButtonState.IDLE) }
    var registerAttempted by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var failedRegisterDialog by remember { mutableStateOf(false) }

    val nameSubject = remember { BehaviorSubject.createDefault("") }
    val emailSubject = remember { BehaviorSubject.createDefault("") }
    val passwordSubject = remember { BehaviorSubject.createDefault("") }
    val compositeDisposable = remember { CompositeDisposable() }
    val registerState by viewModel.registerState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.registerState
    }

    LaunchedEffect(registerState) {
        when (registerState) {
            is ResultState.Initial -> {
                registerButtonState = SSButtonState.IDLE
                registerAttempted = false
            }

            is ResultState.Loading -> {
                registerButtonState = SSButtonState.LOADING
            }

            is ResultState.Success -> {
                registerButtonState = SSButtonState.SUCCESS
                registerAttempted = true
                navController.navigate("login")
            }

            is ResultState.Error -> {
                registerButtonState = SSButtonState.FAILURE
                registerAttempted = true
                val errorMessage = (registerState as ResultState.Error).errorMessage
                if (errorMessage.contains("500")) {
                    failedRegisterDialog = true
                } else {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    registerButtonState = SSButtonState.IDLE
                }, 500)
            }
        }
    }

    DisposableEffect(Unit) {
        val nameDisposable = emailSubject
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { nameInput ->
                if (registerAttempted) {
                    nameError = if (nameInput.isEmpty()) {
                        context.getString(R.string.name_empty_error)
                    } else if (nameInput.length < 3) {
                        context.getString(R.string.name_min_length_error)
                    } else {
                        null
                    }
                }
            }

        val emailObservable = emailSubject
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { emailInput ->
                if (registerAttempted) {
                    emailError = if (emailInput.isEmpty()) {
                        context.getString(R.string.email_empty_error)
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                        context.getString(R.string.email_invalid_error)
                    } else {
                        null
                    }
                }
            }
        val passwordObservable = passwordSubject
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { passwordInput ->
                if (registerAttempted) {
                    passwordError = if (passwordInput.isEmpty()) {
                        context.getString(R.string.password_empty_error)
                    } else if (passwordInput.length < 8) {
                        context.getString(R.string.password_min_length_error)
                    } else {
                        null
                    }
                }
            }

        nameDisposable.let { compositeDisposable.add(it) }
        emailObservable.let { compositeDisposable.add(it) }
        passwordObservable.let { compositeDisposable.add(it) }

        onDispose {
            compositeDisposable.clear()
        }
    }
    Box (modifier = Modifier.fillMaxSize()){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RegisterItem()
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameSubject.onNext(it)
                },
                label = { Text(stringResource(R.string.name_label)) },
                isError = nameError != null,
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
                    color = if (name.isNotEmpty()) textColor else greyTextColor
                )
            )

            if (nameError != null) {
                Text(
                    text = nameError ?: "",
                    color = Color.Red,
                    style = TextStyle(fontSize = 12.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 8.dp)
                )
            }

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
                    text = emailError ?: "",
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
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.hide_password),
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_eye_primary),
                                contentDescription = stringResource(R.string.show_password),
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            )

            if (passwordError != null) {
                Text(
                    text = passwordError ?: "",
                    color = Color.Red,
                    style = TextStyle(fontSize = 12.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 8.dp)
                )
            }

            SSJetPackComposeProgressButton(
                type = SSButtonType.CIRCLE,
                width = 362.dp,
                height = 50.dp,
                cornerRadius = 10,
                assetColor = whiteColor,
                text =  stringResource(R.string.register_button),
                textModifier = Modifier.padding(horizontal = 15.dp, vertical = 16.dp),
                fontSize = 16.sp,
                fontFamily = PoppinsSemiBold,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = whiteColor,
                    disabledContainerColor = primaryColor.copy(alpha = 0.6f)
                ),
                buttonState = registerButtonState,
                onClick = {
                    registerAttempted = true

                    val isNameValid = if (name.isEmpty()) {
                        nameError = context.getString(R.string.name_empty_error)
                        false
                    } else if (name.length < 3) {
                        nameError = context.getString(R.string.name_min_length_error)
                        false
                    } else {
                        nameError = null
                        true
                    }

                    val isEmailValid = if (email.isEmpty()) {
                        emailError = context.getString(R.string.email_empty_error)
                        false
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailError = context.getString(R.string.email_invalid_error)
                        false
                    } else {
                        emailError = null
                        true
                    }

                    val isPasswordValid = if (password.isEmpty()) {
                        passwordError = context.getString(R.string.password_empty_error)
                        false
                    } else if (password.length < 8) {
                        passwordError = context.getString(R.string.password_min_length_error)
                        false
                    } else {
                        passwordError = null
                        true
                    }

                    if (isNameValid && isEmailValid && isPasswordValid) {
                        viewModel.register(name, email, password)
                    }
                },
            )

            TextButton(
                onClick = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .padding(top = 24.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = stringResource(R.string.login_link),
                    color = textColor,
                    fontFamily = PoppinsSemiBold,
                    style = TextStyle(fontSize = 14.sp)
                )
            }
        }

        if (failedRegisterDialog) {
            Dialog(onDismissRequest = { failedRegisterDialog = false }) {
                CustomAlertDialog(
                    title = stringResource(R.string.account_already_exists_title),
                    message = stringResource(R.string.account_already_exists_message),
                    positiveText = stringResource(R.string.login_now),
                    negativeText = stringResource(R.string.cancel),
                    onConfirm = {
                        failedRegisterDialog = false
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onDismiss = {
                        failedRegisterDialog = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Register UI Only")
@Composable
fun RegisterUIPreview() {
    UrVoiceTheme {
        RegisterItem()
    }
}
