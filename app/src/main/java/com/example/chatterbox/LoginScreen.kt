package com.example.chatterbox

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.chatterbox.backend.AuthState
import com.example.chatterbox.backend.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

private const val RC_SIGN_IN = 9001

@Composable
fun LoginScreen(navController: NavHostController, authViewModel: AuthViewModel) {

    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()
    val showSnackbar = { message: String ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    var passwordVisible by remember { mutableStateOf(false) }
    var isEmailValid by remember { mutableStateOf(false) }

    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }

    val authState by authViewModel.authState.observeAsState()
    val userProfile by authViewModel.userProfile.observeAsState()
    val darkModeEnabled = userProfile?.get("isDarkModeOn") as? Boolean ?: false

    val backgroundColor = if (darkModeEnabled) Color(0xFF2E2E2E) else Color.White
    val textColor = if (darkModeEnabled) Color.White else Color.Black
    val secondaryTextColor = if (darkModeEnabled) Color(0xFF929AAB) else Color.Gray
    val inputFieldColor = if (darkModeEnabled) Color.White else Color.Black
    val buttonColor = if (darkModeEnabled) Color.White else Color(0xFF010101)
    val buttonTextColor = if (darkModeEnabled) Color.Black else Color.White

    fun handleLogin() {
        if (userEmail.isNotEmpty() && userPassword.isNotEmpty() && isEmailValid) {
            authViewModel.login(userEmail, userPassword)
        } else {
            showSnackbar("Please fill all fields correctly!")
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                navController.navigate("home")
                showSnackbar("Login successful!")
            }
            is AuthState.Error -> {
                showSnackbar((authState as AuthState.Error).message)
            }
            else -> Unit
        }
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
            .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            LoginLogoSection(darkModeEnabled)
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(top = 30.dp)
            )
            LoginForm(
                userEmail = userEmail,
                onUserEmailChange = {
                    userEmail = it
                    isEmailValid = isValidEmail(it)
                },
                userPassword = userPassword,
                onUserPasswordChange = { userPassword = it },
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                isEmailValid = isEmailValid,
                onSignInClick = {
                    if (isEmailValid && userEmail.isNotEmpty() && userPassword.isNotEmpty()) {
                        handleLogin()
                    } else {
                        showSnackbar("Please fill all fields correctly!")
                    }
                },
                onForgotPasswordClick = { navController.navigate("sendResetEmail") },
                onSignUpClick = { navController.navigate("signup") },
                textColor = textColor,
                secondaryTextColor = secondaryTextColor,
                inputFieldColor = inputFieldColor,
                buttonColor = buttonColor,
                buttonTextColor = buttonTextColor,
                darkModeEnabled,
                navController,
                authViewModel
            )
        }
    }
}

@Composable
fun LoginLogoSection(darkModeEnabled: Boolean) {
    val logoBackgroundColor = if (darkModeEnabled) Color.White else Color.Transparent

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = logoBackgroundColor),
        modifier = Modifier.padding(16.dp)
    ) {
        Column(
            Modifier
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
}

@Composable
fun LoginForm(
    userEmail: String,
    onUserEmailChange: (String) -> Unit,
    userPassword: String,
    onUserPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    isEmailValid: Boolean,
    onSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
    textColor: Color,
    secondaryTextColor: Color,
    inputFieldColor: Color,
    buttonColor: Color,
    buttonTextColor: Color,
    darkModeEnabled: Boolean,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Welcome to ChatterBox",
            color = textColor,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Connect, Converse, Create",
            color = secondaryTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif
        )
        Spacer(Modifier.height(30.dp))

        LoginCustomTextField(
            label = "Email",
            value = userEmail,
            onValueChange = onUserEmailChange,
            leadingIcon = { Icon(Icons.Rounded.Email, modifier = Modifier.size(22.dp), tint = inputFieldColor, contentDescription = "") },
            placeholder = "Enter your email address",
            trailingIcon = {
                if (userEmail.isNotEmpty()) {
                    Icon(
                        if (isEmailValid) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                        tint = if (isEmailValid) Color(0xFF57C75B) else Color.Red,
                        contentDescription = "",
                        modifier = Modifier.size(16.dp)
                    )
                }
            },
            textColor = textColor,
            inputFieldColor = inputFieldColor
        )
        Spacer(Modifier.height(12.dp))

        LoginCustomTextField(
            label = "Password",
            value = userPassword,
            onValueChange = onUserPasswordChange,
            leadingIcon = { Icon(Icons.Rounded.Password, modifier = Modifier.size(22.dp), tint = inputFieldColor, contentDescription = "") },
            placeholder = "Enter your password",
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        tint = inputFieldColor,
                        contentDescription = ""
                    )
                }
            },
            textColor = textColor,
            inputFieldColor = inputFieldColor
        )
        Spacer(Modifier.height(4.dp))

        Text(
            text = "Forgot Password",
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onForgotPasswordClick() })
                }
                .padding(start = 4.dp, bottom = 8.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            textAlign = TextAlign.End,
            color = secondaryTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(12.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            onClick = onSignInClick,
            shape = RoundedCornerShape(size = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = buttonTextColor
            )
        ) {
            Text(
                "Sign In",
                fontSize = 18.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Divider(modifier = Modifier.weight(1f))
            Text(" or ", color = if (darkModeEnabled) Color(0xFFFFFFFF) else Color.Black)
            Divider(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))
        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            onClick = {
                val googleSignInClient = authViewModel.getGoogleSignInClient(navController.context as Activity)
                val signInIntent = googleSignInClient.signInIntent
                (navController.context as Activity).startActivityForResult(signInIntent, RC_SIGN_IN)
            },
            shape = RoundedCornerShape(size = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonTextColor,
                contentColor = buttonColor
            )
        ) {
            Icon(modifier = Modifier.size(26.dp), painter = painterResource(id = R.drawable.google_144), contentDescription = "")

            Spacer(Modifier.width(10.dp))
            Text(
                "Sign In with Google",
                fontSize = 18.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Don't have an account yet? ",
                color = secondaryTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )

            Text(
                modifier = Modifier.clickable(onClick = onSignUpClick),
                text = "Sign Up",
                color = inputFieldColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginCustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    textColor: Color,
    inputFieldColor: Color
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
            placeholder = { Text(placeholder, color = inputFieldColor.copy(alpha = 0.5f)) },
            visualTransformation = visualTransformation,
            isError = isError,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = inputFieldColor,
                unfocusedBorderColor = inputFieldColor,
                cursorColor = inputFieldColor,
                textColor = textColor
            )
        )
    }
}
