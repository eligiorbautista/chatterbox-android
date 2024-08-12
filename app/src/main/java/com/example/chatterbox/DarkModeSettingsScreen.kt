package com.example.chatterbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
fun DarkModeSettings(navController: NavHostController, authViewModel: AuthViewModel) {
    val userProfile by authViewModel.userProfile.observeAsState()
    var darkModeEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            darkModeEnabled = it["isDarkModeOn"] as? Boolean ?: false
        }
    }

    val lightColors = lightColorScheme(
        primary = Color.White,
        onPrimary = Color.Black,
        surface = Color.White,
        onSurface = Color.Black
    )

    val darkColors = darkColorScheme(
        primary = Color(0xFF2E2E2E),
        onPrimary = Color.White,
        surface = Color(0xFF2E2E2E),
        onSurface = Color.White
    )

    val colors = if (darkModeEnabled) darkColors else lightColors

    MaterialTheme(colorScheme = colors) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Dark Mode", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                item {
                    ElevatedCard(
                        elevation = CardDefaults.cardElevation(),
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
                            .clickable {
                                darkModeEnabled = true
                                authViewModel.updateDarkModeSetting(true)
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                text = "On",
                                modifier = Modifier.padding(start = 8.dp),
                                fontSize = 18.sp
                            )

                            Spacer(Modifier.weight(1f))

                            RadioButton(
                                selected = darkModeEnabled,
                                onClick = {
                                    darkModeEnabled = true
                                    authViewModel.updateDarkModeSetting(true)
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = colors.onPrimary
                                )
                            )
                        }
                    }
                }
                item {
                    ElevatedCard(
                        elevation = CardDefaults.cardElevation(),
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
                            .clickable {
                                darkModeEnabled = false
                                authViewModel.updateDarkModeSetting(false)
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                text = "Off",
                                modifier = Modifier.padding(start = 8.dp),
                                fontSize = 18.sp
                            )

                            Spacer(Modifier.weight(1f))

                            RadioButton(
                                selected = !darkModeEnabled,
                                onClick = {
                                    darkModeEnabled = false
                                    authViewModel.updateDarkModeSetting(false)
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = colors.onPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
