package com.example.chatterbox

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showSnackbar = { message: String ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isEmailValid by remember { mutableStateOf(false) }
    var isPasswordMatch by remember { mutableStateOf(true) }

    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

    val authState by authViewModel.authState.observeAsState()
    val userProfile by authViewModel.userProfile.observeAsState()

    val darkModeEnabled = userProfile?.get("isDarkModeOn") as? Boolean ?: false
    val backgroundColor = if (darkModeEnabled) Color(0xFF2E2E2E) else Color.White
    val textColor = if (darkModeEnabled) Color.White else Color.Black
    val secondaryTextColor = if (darkModeEnabled) Color(0xFF929AAB) else Color.Gray
    val inputFieldColor = if (darkModeEnabled) Color.White else Color.Black
    val buttonColor = if (darkModeEnabled) Color.White else Color(0xFF010101)
    val buttonTextColor = if (darkModeEnabled) Color.Black else Color.White

    fun handleRegistration() {
        if (userEmail.isNotEmpty() && userPassword.isNotEmpty() && confirmPassword.isNotEmpty() && displayName.isNotEmpty()) {
            if (userPassword == confirmPassword) {
                authViewModel.register(userEmail, userPassword, displayName)
                showSnackbar("Loading... Please wait.")
                userEmail = ""
                userPassword = ""
                confirmPassword = ""
                displayName = ""
            } else {
                showSnackbar("Passwords do not match!")
            }
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
            RegistrationLogoSection(darkModeEnabled)
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(top = 30.dp)
            )
            RegistrationForm(
                userEmail = userEmail,
                onUserEmailChange = {
                    userEmail = it
                    isEmailValid = isValidEmail(it)
                },
                userPassword = userPassword,
                onUserPasswordChange = {
                    userPassword = it
                    isPasswordMatch = userPassword == confirmPassword
                },
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = {
                    confirmPassword = it
                    isPasswordMatch = userPassword == confirmPassword
                },
                displayName = displayName,
                onDisplayNameChange = { displayName = it },
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                confirmPasswordVisible = confirmPasswordVisible,
                onConfirmPasswordVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible },
                isEmailValid = isEmailValid,
                isPasswordMatch = isPasswordMatch,
                onSignUpClick = {
                    if (isEmailValid && isPasswordMatch && displayName.isNotEmpty()) {
                        handleRegistration()
                    } else {
                        showSnackbar("Please fill all fields correctly!")
                    }
                },
                onSignInClick = { navController.navigate("signin") },
                textColor = textColor,
                inputFieldColor = inputFieldColor,
                buttonColor = buttonColor,
                buttonTextColor = buttonTextColor
            )
        }
    }
}




@Composable
fun RegistrationLogoSection(darkModeEnabled: Boolean) {
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
fun RegistrationForm(
    userEmail: String,
    onUserEmailChange: (String) -> Unit,
    userPassword: String,
    onUserPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    confirmPasswordVisible: Boolean,
    onConfirmPasswordVisibilityChange: () -> Unit,
    isEmailValid: Boolean,
    isPasswordMatch: Boolean,
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    textColor: Color,
    inputFieldColor: Color,
    buttonColor: Color,
    buttonTextColor: Color

) {
    Column(
        Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Create an Account",
            color = textColor,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Join ChatterBox and start connecting",
            color = Color(0xFF929AAB),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif
        )
        Spacer(Modifier.height(40.dp))

        Text(
            text = "Display name",
            Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 8.dp),
            textAlign = TextAlign.Start,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        CustomTextField(
            value = displayName,
            onValueChange = onDisplayNameChange,
            leadingIcon = { Icon(Icons.Rounded.Person, tint = textColor, contentDescription = "") },
            placeholder = "Enter your display name",
            inputFieldColor = inputFieldColor,
            textColor = textColor
        )
        Spacer(Modifier.height(20.dp))

        Text(
            text = "Email",
            Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 8.dp),
            textAlign = TextAlign.Start,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        CustomTextField(
            value = userEmail,
            onValueChange = onUserEmailChange,
            leadingIcon = { Icon(Icons.Rounded.Email, tint = textColor, contentDescription = "") },
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
            inputFieldColor = inputFieldColor,
            textColor = textColor
        )
        Spacer(Modifier.height(20.dp))

        Text(
            text = "Password",
            Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 8.dp),
            textAlign = TextAlign.Start,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        CustomTextField(
            value = userPassword,
            onValueChange = onUserPasswordChange,
            leadingIcon = { Icon(Icons.Rounded.Password, tint = textColor, contentDescription = "") },
            placeholder = "Enter your password",
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        tint = textColor,
                        contentDescription = ""
                    )
                }
            },
            inputFieldColor = inputFieldColor,
            textColor = textColor
        )
        Spacer(Modifier.height(20.dp))

        Text(
            text = "Confirm password",
            Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 8.dp),
            textAlign = TextAlign.Start,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        CustomTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            leadingIcon = { Icon(Icons.Rounded.Password, tint = textColor, contentDescription = "") },
            placeholder = "Re-enter your password",
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onConfirmPasswordVisibilityChange) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        tint = textColor,
                        contentDescription = ""
                    )
                }
            },
            isError = !isPasswordMatch && confirmPassword.isNotEmpty(),
            inputFieldColor = inputFieldColor,
            textColor = textColor
        )

        if (!isPasswordMatch && confirmPassword.isNotEmpty()) {
            Text(
                text = "Passwords do not match",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
        }

        Spacer(Modifier.height(30.dp))

        Button(
            onClick = onSignUpClick,
            shape = RoundedCornerShape(size = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = buttonTextColor
            )
        ) {
            Text(
                "Sign Up",
                color = buttonTextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Already have an account?",
                color = textColor,
                fontSize = 14.sp
            )
            Spacer(Modifier.width(2.dp))
            TextButton(onClick = onSignInClick) {
                Text(
                    "Sign In",
                    color = buttonColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    placeholder: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    inputFieldColor: Color,
    textColor: Color
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        placeholder = { Text(placeholder) },
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

