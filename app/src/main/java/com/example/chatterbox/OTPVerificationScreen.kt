package com.example.chatterbox

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.chatterbox.backend.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val userProfile by authViewModel.userProfile.observeAsState()
    val darkModeEnabled = userProfile?.get("isDarkModeOn") as? Boolean ?: false
    val backgroundColor = if (darkModeEnabled) Color(0xFF2E2E2E) else Color.White
    val textColor = if (darkModeEnabled) Color.White else Color.Black

    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()
    val showSnackbar = { message: String ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    var otp by remember { mutableStateOf("") }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
            .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            OtpLogoSection(darkModeEnabled)
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(top = 30.dp)
            )
            OtpForm(
                otp = otp,
                onOtpChange = { otp = it },
                onVerifyClick = {
                    if (otp.length == 6) {
                        navController.navigate("home")
                    } else {
                        showSnackbar("Please enter a valid OTP!")
                    }
                },
                onResendOtpClick = { showSnackbar("OTP resent!") },
                textColor = textColor,
                darkModeEnabled
            )
        }
    }
}

@Composable
fun OtpLogoSection(darkModeEnabled: Boolean) {
    val logoBackgroundColor = if (darkModeEnabled) Color.White else Color.Transparent
    Column(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(logoBackgroundColor)
            .padding(top = 20.dp, start = 20.dp, end = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_text_no_background),
            contentDescription = "",
            Modifier.width(230.dp)
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun OtpForm(
    otp: String,
    onOtpChange: (String) -> Unit,
    onVerifyClick: () -> Unit,
    onResendOtpClick: () -> Unit,
    textColor: Color,
    darkModeEnabled : Boolean
) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Enter OTP",
            color = textColor,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "A One-Time Password has been sent to your email.",
            color = textColor.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif
        )
        Spacer(Modifier.height(30.dp))

        OTPCustomTextField(
            label = "OTP",
            value = otp,
            onValueChange = onOtpChange,
            leadingIcon = { Icon(Icons.Rounded.Lock, tint = textColor, contentDescription = "") },
            placeholder = "Enter the OTP",
            visualTransformation = VisualTransformation.None,
            isError = otp.isNotEmpty() && otp.length != 6,
            textColor = textColor
        )
        Spacer(Modifier.height(16.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            onClick = onVerifyClick,
            shape = RoundedCornerShape(size = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF010101),
                contentColor = Color.White
            )
        ) {
            Text(
                "Verify",
                fontSize = 18.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Didn't receive the OTP? ",
                color = textColor.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )

            Text(
                modifier = Modifier.clickable(onClick = onResendOtpClick),
                text = "Resend OTP",
                color = if (darkModeEnabled) Color(0xFFD3D3D3) else Color(0xFF010101),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPCustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    textColor: Color
) {
    Column {
        Text(
            text = label,
            Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 8.dp),
            textAlign = TextAlign.Start,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            value = value,
            onValueChange = onValueChange,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            placeholder = { Text(placeholder, color = textColor.copy(alpha = 0.5f)) },
            visualTransformation = visualTransformation,
            isError = isError,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = textColor,
                unfocusedBorderColor = textColor,
                cursorColor = textColor,
                textColor = textColor
            )
        )
    }
}


