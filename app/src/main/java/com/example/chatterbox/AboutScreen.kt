package com.example.chatterbox

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.chatterbox.backend.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val userProfile by authViewModel.userProfile.observeAsState()
    val darkModeEnabled = userProfile?.get("isDarkModeOn") as? Boolean ?: false
    val backgroundColor = if (darkModeEnabled) Color(0xFF2E2E2E) else Color.White
    val textColor = if (darkModeEnabled) Color.White else Color.Black

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About", fontWeight = FontWeight.SemiBold, color = textColor) },
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
                if (darkModeEnabled) {
                    Card(
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_text_no_background),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(180.dp).padding(horizontal = 20.dp)
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.logo_text_no_background),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(180.dp)
                    )
                }

                Text(
                    text = "About ChatterBox",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                    color = textColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "ChatterBox is a real-time chat app designed to make communication seamless.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                    color = textColor
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Text(
                    text = "Key Features",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                    color = textColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = """
                        • Real-time messaging
                        • User-friendly interface
                        • Secure authentication
                        • Profile management
                        • Push notifications
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                    color = textColor
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Text(
                    text = "Technical Highlights",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Built with Kotlin, Jetpack Compose, and Firebase for a smooth and secure experience.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                    color = textColor
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Version: 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
                Text(
                    text = "Developed by Eli Bautista",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = textColor
                )
            }
        }
    }
}
