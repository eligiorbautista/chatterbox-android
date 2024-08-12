package com.example.chatterbox

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
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

@Composable
fun SendResetEmailScreen(navController: NavHostController, authViewModel: AuthViewModel) {
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

    var email by remember { mutableStateOf("") }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
            .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            SendResetEmailLogoSection(darkModeEnabled)
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(top = 30.dp)
            )
            SendResetEmailForm(
                email = email,
                onOtpChange = { email = it },
                onVerifyClick = {
                    if (isValidEmail(email)) {
                        showSnackbar("Reset link has been sent to your inbox.")
                        authViewModel.sendPasswordResetEmail(email)
                    } else {
                        showSnackbar("Please enter a valid email!")
                    }
                },
                onResendClick = { showSnackbar("Reset link resent!") },
                textColor = textColor,
                darkModeEnabled
            )
        }
    }
}

@Composable
fun SendResetEmailLogoSection(darkModeEnabled: Boolean) {
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
fun SendResetEmailForm(
    email: String,
    onOtpChange: (String) -> Unit,
    onVerifyClick: () -> Unit,
    onResendClick: () -> Unit,
    textColor: Color,
    darkModeEnabled : Boolean
) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Account Recovery",
            color = textColor,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "If your account exists we will be sending a link to reset your password.",
            color = textColor.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif
        )
        Spacer(Modifier.height(30.dp))

        SendResetEmailCustomTextField(
            label = "Email",
            value = email,
            onValueChange = onOtpChange,
            leadingIcon = { Icon(Icons.Rounded.Email, tint = textColor, contentDescription = "") },
            placeholder = "Enter your email",
            visualTransformation = VisualTransformation.None,
            isError = email.isNotEmpty() && isValidEmail(email),
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
                "Send",
                fontSize = 18.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Didn't receive an email? ",
                color = textColor.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )

            Text(
                modifier = Modifier.clickable(onClick = onResendClick),
                text = "Resend",
                color = if (darkModeEnabled) Color(0xFFD3D3D3) else Color(0xFF010101),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendResetEmailCustomTextField(
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
