// ChatsScreen.kt
package com.example.chatterbox

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.chatterbox.backend.AuthViewModel

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    var searchQuery by remember { mutableStateOf("") }

    // Observe the user profile to determine if dark mode is enabled
    val userProfile by authViewModel.userProfile.observeAsState()
    val darkModeEnabled = userProfile?.get("isDarkModeOn") as? Boolean ?: false
    val backgroundColor = if (darkModeEnabled) Color(0xFF2E2E2E) else Color.White
    val textColor = if (darkModeEnabled) Color.White else Color.Black
    val cardBackgroundColor = if (darkModeEnabled) Color(0xFF2E2E2E) else Color(0xFFFFFFFF)

    // Mock data for users and last chats
    val chatList = listOf(
        ChatData("John Doe", "Hey, how are you?", painterResource(id = R.drawable.default_avatar)),
        ChatData("Jane Smith", "Can we meet tomorrow?", painterResource(id = R.drawable.default_avatar)),
        ChatData("Alice Johnson", "Check this out!", painterResource(id = R.drawable.default_avatar)),
        ChatData("Bob Brown", "I'll call you later.", painterResource(id = R.drawable.default_avatar)),
        ChatData("Charlie Davis", "Got it, thanks!", painterResource(id = R.drawable.default_avatar)),
        ChatData("Diana Evans", "Happy Birthday!", painterResource(id = R.drawable.default_avatar)),
        ChatData("Edward Harris", "Let's catch up soon.", painterResource(id = R.drawable.default_avatar)),
        ChatData("Fiona Adams", "What do you think?", painterResource(id = R.drawable.default_avatar)),
        ChatData("George Clarke", "I'm on my way.", painterResource(id = R.drawable.default_avatar)),
        ChatData("Hannah Scott", "See you at the event.", painterResource(id = R.drawable.default_avatar))
    )

    val filteredChatList = remember(searchQuery) {
        chatList.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.lastMessage.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 0.dp)
            .background(backgroundColor)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .height(56.dp),
            label = { Text("Search...", color = textColor) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = textColor)
            },
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = textColor,
                unfocusedBorderColor = textColor,
                cursorColor = textColor,
                textColor = textColor
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(filteredChatList) { chatData ->
                ChatItem(
                    name = chatData.name,
                    lastMessage = chatData.lastMessage,
                    painter = chatData.painter,
                    onClick = { navController.navigate("chatDetail/${chatData.name}") },
                    textColor = textColor,
                    cardBackgroundColor = cardBackgroundColor
                )
            }
        }
    }
}

@Composable
fun ChatItem(name: String, lastMessage: String, painter: Painter, onClick: () -> Unit, textColor: Color, cardBackgroundColor: Color) {
    Card(
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
            Column(
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = textColor
                )
                Text(
                    text = lastMessage,
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

data class ChatData(
    val name: String,
    val lastMessage: String,
    val painter: Painter
)
