package com.firman.capstone.urvoice.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.firman.capstone.urvoice.ui.theme.UrVoiceTheme
import com.firman.capstone.urvoice.R
import com.firman.capstone.urvoice.ui.theme.PoppinsRegular
import com.firman.capstone.urvoice.ui.theme.PoppinsSemiBold
import com.firman.capstone.urvoice.ui.theme.textColor
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.firman.capstone.urvoice.ui.theme.primaryColor
import com.firman.capstone.urvoice.ui.theme.whiteBackground
import com.firman.capstone.urvoice.ui.theme.whiteColor
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonType
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSJetPackComposeProgressButton

data class SignModel(
    val title: String,
    val description: String,
    val imagesRes: Int,
)

@Composable
fun SignItem(page: SignModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = page.imagesRes),
            contentDescription = "Image Login",
            modifier = Modifier
                .height(314.dp)
                .width(314.dp)
        )
        Text(
            text = page.title,
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 19.dp),
            style = TextStyle(
                fontFamily = PoppinsSemiBold,
                fontSize = 20.sp,
                color = primaryColor,
                textAlign = TextAlign.Center,
            )
        )
        Text(
            text = page.description,
            modifier = Modifier.padding(horizontal = 15.dp),
            style = TextStyle(
                fontFamily = PoppinsRegular,
                fontSize = 12.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun SignScreen(navController: NavController) {
    val signModel = SignModel(
        title = stringResource(R.string.sign_title),
        description = stringResource(R.string.sign_desc),
        imagesRes = R.drawable.iv_login
    )
    var loginButtonState by remember { mutableStateOf(SSButtonState.IDLE) }
    var registerButtonState by remember { mutableStateOf(SSButtonState.IDLE) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SignItem(page = signModel)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            SSJetPackComposeProgressButton(
                type = SSButtonType.CIRCLE,
                width = 157.dp,
                height = 50.dp,
                cornerRadius = 100,
                assetColor = whiteColor,
                text = "Login",
                textModifier = Modifier.padding(horizontal = 8.dp),
                fontSize = 14.sp,
                fontFamily = PoppinsSemiBold,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = whiteColor,
                    disabledContainerColor = primaryColor.copy(alpha = 0.5f)
                ),
                buttonState = loginButtonState,
                onClick = {
                    loginButtonState = SSButtonState.LOADING
                    navController.navigate("login")
                    loginButtonState = SSButtonState.IDLE
                }
            )

            SSJetPackComposeProgressButton(
                type = SSButtonType.CIRCLE,
                width = 157.dp,
                height = 50.dp,
                cornerRadius = 100,
                assetColor = primaryColor,
                buttonBorderColor = primaryColor,
                buttonBorderWidth = 2.dp,
                text = "Register",
                textModifier = Modifier.padding(horizontal = 8.dp),
                fontSize = 14.sp,
                fontFamily = PoppinsSemiBold,
                colors = ButtonDefaults.buttonColors(
                    containerColor = whiteBackground,
                    contentColor = primaryColor
                ),
                buttonState = registerButtonState,
                onClick = {
                    registerButtonState = SSButtonState.LOADING
                    navController.navigate("register")
                    registerButtonState = SSButtonState.IDLE
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignScreenPreview() {
    UrVoiceTheme {
        SignScreen(
            navController = NavController(LocalContext.current)
        )
    }
}