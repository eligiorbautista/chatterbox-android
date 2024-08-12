package com.example.chatterbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
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
fun SecuritySettingsScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val userProfile by authViewModel.userProfile.observeAsState()
    val darkModeEnabled = userProfile?.get("isDarkModeOn") as? Boolean ?: false
    val backgroundColor = if (darkModeEnabled) Color(0xFF2E2E2E) else Color.White
    val textColor = if (darkModeEnabled) Color.White else Color.Black

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Password and security", fontWeight = FontWeight.SemiBold, color = textColor) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .background(backgroundColor),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                        .clickable { navController.navigate("changePassword") },
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = backgroundColor
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Change password",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = "",
                            modifier = Modifier.size(20.dp),
                            tint = textColor
                        )
                    }
                }
            }

            item {
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                        .clickable { navController.navigate("twoFactorAuthSettings") },
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = backgroundColor
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Two-factor authentication",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = "",
                            modifier = Modifier.size(20.dp),
                            tint = textColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
