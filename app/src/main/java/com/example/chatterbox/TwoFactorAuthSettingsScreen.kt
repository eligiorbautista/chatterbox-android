package com.example.chatterbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.chatterbox.backend.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFactorAuthSettingsScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val userProfile by authViewModel.userProfile.observeAsState()
    var twoFactorEnabled by remember { mutableStateOf(false) }
    val darkModeEnabled = userProfile?.get("isDarkModeOn") as? Boolean ?: false
    val backgroundColor = if (darkModeEnabled) Color(0xFF2E2E2E) else Color.White
    val textColor = if (darkModeEnabled) Color.White else Color.Black

    LaunchedEffect(userProfile) {
        twoFactorEnabled = userProfile?.get("isTwoFactorAuthenticationOn") as? Boolean ?: false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Two-factor authentication", fontWeight = FontWeight.SemiBold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .background(backgroundColor)
        ) {
            Text(
                "Two-factor authentication protects your account by requiring an additional code when you log in.",
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 6.dp),
                color = textColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedCard(
                elevation = CardDefaults.cardElevation(),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (twoFactorEnabled) "On" else "Off",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = twoFactorEnabled,
                        onCheckedChange = {
                            twoFactorEnabled = it
                            authViewModel.updateTwoFactorAuthenticationSetting(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color.LightGray,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.White
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "We'll send a code to the email registered to your account.",
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 6.dp),
                color = textColor
            )
        }
    }
}
