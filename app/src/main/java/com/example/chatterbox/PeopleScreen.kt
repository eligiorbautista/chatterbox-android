package com.example.chatterbox

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
import androidx.compose.material.icons.rounded.Circle
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
import coil.compose.rememberImagePainter
import com.example.chatterbox.backend.AuthViewModel
import com.example.chatterbox.backend.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(navController: NavHostController, userId: String?, authViewModel: AuthViewModel) {
    val userProfile by authViewModel.userProfile.observeAsState()
    val allUsers by authViewModel.allUsers.observeAsState(emptyList())
    val darkModeEnabled = userProfile?.get("isDarkModeOn") as? Boolean ?: false
    val currentUserId = authViewModel.getUserId()

    val backgroundColor = if (darkModeEnabled) Color(0xFF2E2E2E) else Color.White
    val textColor = if (darkModeEnabled) Color.White else Color.Black
    val cardBackgroundColor = if (darkModeEnabled) Color(0xFF2E2E2E) else Color.White
    val statusTextColor = if (darkModeEnabled) Color.LightGray else Color.Gray

    var searchQuery by remember { mutableStateOf("") }

    val filteredPeopleList = remember(searchQuery, allUsers, currentUserId) {
        allUsers.filter {
            it.uid != currentUserId && it.displayName.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.fetchAllUsers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 0.dp)
            .background(backgroundColor)
    ) {
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

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredPeopleList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You have no contacts at the moment",
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyColumn {
                items(filteredPeopleList) { personData ->
                    PersonItem(
                        name = personData.displayName,
                        isActive = personData.isOnline,
                        profilePictureUrl = personData.profilePictureUrl,
                        onClick = { navController.navigate("chatDetail/${personData.uid}") },
                        cardBackgroundColor = cardBackgroundColor,
                        textColor = textColor,
                        statusTextColor = statusTextColor
                    )
                }
            }
        }
    }
}



@Composable
fun PersonItem(
    name: String,
    isActive: Boolean,
    profilePictureUrl: String,
    onClick: () -> Unit,
    cardBackgroundColor: Color,
    textColor: Color,
    statusTextColor: Color
) {
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
                painter = rememberImagePainter(profilePictureUrl),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Circle,
                        contentDescription = null,
                        tint = if (isActive) Color(0xFF57C75B) else Color(0xFF9C9C9C),
                        modifier = Modifier.size(9.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isActive) "Active Now" else "Offline",
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = statusTextColor
                    )
                }
            }
        }
    }
}
