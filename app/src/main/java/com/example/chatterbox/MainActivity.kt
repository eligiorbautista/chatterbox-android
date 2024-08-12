package com.example.chatterbox

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatterbox.backend.AuthState
import com.example.chatterbox.backend.AuthViewModel
import com.example.chatterbox.ui.theme.ChatterBoxTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp(authViewModel = authViewModel)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    authViewModel.firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                authViewModel.updateAuthState(AuthState.Error("Google sign-in failed: ${e.message}"))
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun MyApp(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val userProfileState = authViewModel.userProfile.observeAsState()
    val userProfile = userProfileState.value
    val darkModeEnabled = userProfile?.get("isDarkModeOn") as? Boolean ?: false

    ChatterBoxTheme(darkTheme = darkModeEnabled) {
        Surface(color = if (darkModeEnabled) Color(0xFF2E2E2E) else Color.White) {
            NavHost(navController = navController, startDestination = "landing") {
                composable("landing") { LandingScreen(navController, authViewModel) }
                composable("signin") { LoginScreen(navController, authViewModel) }
                composable("signup") { RegistrationScreen(navController, authViewModel) }
                composable("home") { HomeScreen(navController, authViewModel) }
                composable("profile") { PersonalProfileScreen(navController, authViewModel) }
                composable("settings") { SettingsScreen(navController, authViewModel) }
                composable("about") { AboutScreen(navController, authViewModel) }
                composable("notificationsSettings") { NotificationsSettingsScreen(navController, authViewModel) }
                composable("securitySettings") { SecuritySettingsScreen(navController, authViewModel) }
                composable("twoFactorAuthSettings") { TwoFactorAuthSettingsScreen(navController, authViewModel) }
                composable("darkModeSettings") { DarkModeSettings(navController, authViewModel) }
                composable("changePassword") { ChangePasswordScreen(navController, authViewModel) }
                composable("chatDetail/{userName}") { backStackEntry ->
                    ConversationScreen(
                        navController,
                        userName = backStackEntry.arguments?.getString("userName") ?: "", authViewModel
                    )
                }
                composable("userProfile/{userName}") { backStackEntry ->
                    UserProfileScreen(
                        userName = backStackEntry.arguments?.getString("userName") ?: "",
                        onCloseClick = { navController.navigateUp() }
                    )
                }
                composable("otp") { OtpScreen(navController, authViewModel) }
                composable("sendResetEmail") { SendResetEmailScreen(navController, authViewModel) }
            }
        }
    }
}
