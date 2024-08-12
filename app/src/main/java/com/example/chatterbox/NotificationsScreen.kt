package com.example.chatterbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
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
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*

data class NotificationData(val title: String, val message: String, val timestamp: String, val userName: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val userProfile by authViewModel.userProfile.observeAsState()
    val darkModeEnabled = userProfile?.get("isDarkModeOn") as? Boolean ?: false

    val backgroundColor = if (darkModeEnabled) Color(0xFF2E2E2E) else Color.White
    val textColor = if (darkModeEnabled) Color.White else Color.Black
    val cardBackgroundColor = if (darkModeEnabled) Color(0xFF3E3E3E) else Color.White
    val iconColor = if (darkModeEnabled) Color.White else Color.Black
    val timestampColor = if (darkModeEnabled) Color.LightGray else Color.Gray

    var notifications by remember { mutableStateOf<List<NotificationData>>(emptyList()) }
    var notificationsListener: ListenerRegistration? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        notificationsListener = authViewModel.db.collection("notifications")
            .whereEqualTo("uid", authViewModel.getUserId())
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    notifications = snapshot.documents.mapNotNull { document ->
                        val title = document.getString("event") ?: ""
                        val message = document.getString("description") ?: ""
                        val timestamp = document.getString("timestamp") ?: ""
                        val userName = document.getString("uid") ?: ""

                        NotificationData(
                            title = title,
                            message = message,
                            timestamp = timestamp,
                            userName = userName
                        )
                    }.reversed()
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 0.dp)
            .background(backgroundColor)
    ) {
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                Text(
                    text = "No notifications to show",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        } else {
            LazyColumn {
                items(notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        cardBackgroundColor = cardBackgroundColor,
                        textColor = textColor,
                        iconColor = iconColor,
                        timestampColor = timestampColor
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            notificationsListener?.remove()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationItem(
    notification: NotificationData,
    cardBackgroundColor: Color,
    textColor: Color,
    iconColor: Color,
    timestampColor: Color
) {
    Card(
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (notification.title == "New Message") Icons.Default.Message else Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = iconColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.timestamp,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = timestampColor
                )
            }
        }
    }
}
