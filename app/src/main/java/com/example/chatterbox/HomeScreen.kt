package com.example.chatterbox

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.chatterbox.backend.AuthState
import com.example.chatterbox.backend.AuthViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navControllerBottom = rememberNavController()
    val currentBackStackEntry by navControllerBottom.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "chats"

    val authState = authViewModel.authState.observeAsState()
    val userProfile by authViewModel.userProfile.observeAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    val showSnackbar = { message: String ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    val userId = authViewModel.getUserId()


    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("signin")
            else -> Unit
        }
    }

    LaunchedEffect(authState.value) {
        authViewModel.checkAuthStatus()
    }

    val topBarTitle = when (currentRoute) {
        "chats" -> "Chats"
        "people" -> "People"
        "notifications" -> "Notifications"
        else -> "Chats"
    }

    val darkModeEnabled = userProfile?.get("isDarkModeOn") as? Boolean ?: false
    val backgroundColor = if (darkModeEnabled) Color(0xFF2E2E2E) else Color.White
    val textColor = if (darkModeEnabled) Color.White else Color.Black

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Surface(
                modifier = Modifier.width(360.dp),
                color = backgroundColor
            ) {
                DrawerContent(
                    navController = navController,
                    closeDrawer = { coroutineScope.launch { drawerState.close() } },
                    authViewModel = authViewModel,
                    userProfile = userProfile,
                    textColor = textColor
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = topBarTitle, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, color = textColor) },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = textColor)
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = backgroundColor
                    )
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(top = 30.dp)
                )
            },
            bottomBar = { BottomNavigationBar(navControllerBottom, darkModeEnabled) },
            containerColor = backgroundColor
        ) { innerPadding ->
            Box(modifier = Modifier
                .padding(innerPadding)
                .background(backgroundColor)) {
                NavHost(
                    navController = navControllerBottom,
                    startDestination = "chats",
                ) {
                    composable("chats") { ChatsScreen(navController, authViewModel) }
                    composable("people") { PeopleScreen(navController, userId = userId, authViewModel)}
                    composable("notifications") { NotificationsScreen(navController, authViewModel) }
                    composable("chatDetail/{userName}") { backStackEntry ->
                        ConversationScreen(
                            navController,
                            userName = backStackEntry.arguments?.getString("userName") ?: "",
                            authViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationDrawerItem(icon: ImageVector, label: String, selected: Boolean = false, onClick: () -> Unit, textColor: Color) {
    val backgroundColor = if (selected) Color.Gray else Color.Transparent

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = textColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, fontSize = 18.sp, color = textColor)
    }
}

@Composable
fun DrawerContent(
    navController: NavHostController,
    closeDrawer: () -> Unit,
    authViewModel: AuthViewModel,
    userProfile: Map<String, Any>?,
    textColor: Color
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Menu",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            IconButton(onClick = { closeDrawer() }) {
                Icon(Icons.Filled.Close, contentDescription = "Close Drawer", tint = textColor)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        userProfile?.let {
            DrawerProfileUserInfo(
                displayName = it["displayName"] as? String ?: "N/A",
                email = it["email"] as? String ?: "N/A",
                profilePictureUrl = it["profilePictureUrl"] as? String,
                onProfileClick = {
                    closeDrawer()
                    navController.navigate("profile")
                },
                textColor = textColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Divider(modifier = Modifier.padding(vertical = 8.dp), color = textColor)

        NavigationDrawerItem(
            icon = Icons.AutoMirrored.Rounded.Chat,
            label = "Chats",
            onClick = {
                closeDrawer()
                navController.navigate("home")
            },
            textColor = textColor
        )

        NavigationDrawerItem(
            icon = Icons.Rounded.Info,
            label = "About",
            onClick = {
                closeDrawer()
                navController.navigate("about")
            },
            textColor = textColor
        )

        NavigationDrawerItem(
            icon = Icons.Rounded.Settings,
            label = "Settings",
            onClick = {
                closeDrawer()
                navController.navigate("settings")
            },
            textColor = textColor
        )

        Spacer(modifier = Modifier.weight(1f))

        NavigationDrawerItem(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            label = "Sign Out",
            onClick = {
                closeDrawer()
                authViewModel.logout()
            },
            textColor = textColor
        )
    }
}

@Composable
fun DrawerProfileUserInfo(
    displayName: String,
    email: String,
    profilePictureUrl: String?,
    onProfileClick: () -> Unit,
    textColor: Color
) {
    val defaultProfilePictureUrl = "https://i.pinimg.com/736x/0d/64/98/0d64989794b1a4c9d89bff571d3d5842.jpg"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onProfileClick)
            .padding(10.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(profilePictureUrl ?: defaultProfilePictureUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .build()
            ),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = displayName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
            Text(text = email, color = textColor.copy(alpha = 0.7f), fontSize = 16.sp)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, darkModeEnabled: Boolean) {
    val items = listOf(
        BottomNavItem("chats", Icons.AutoMirrored.Rounded.Chat, "Chats"),
        BottomNavItem("people", Icons.Rounded.People, "People"),
        BottomNavItem("notifications", Icons.Rounded.Notifications, "Notifications")
    )

    val containerColor = if (darkModeEnabled) Color(0xFF2E2E2E) else Color(0xFFFFFFFF)
    val selectedIconColor = if (darkModeEnabled) Color(0xFF000000) else Color(0xFFFFFFFF)
    val selectedTextColor = if (darkModeEnabled) Color(0xFFFFFFFF) else Color(0xFF030303)
    val unselectedIconColor = if (darkModeEnabled) Color(0xFFB0B0B0) else Color(0xFF000000)
    val unselectedTextColor = if (darkModeEnabled) Color(0xFFB0B0B0) else Color(0xFF000000)
    val indicatorColor = if (darkModeEnabled) Color(0xFFFFFFFF) else Color(0xFF000000)

    NavigationBar(
        containerColor = containerColor
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedIconColor,
                    selectedTextColor = selectedTextColor,
                    unselectedIconColor = unselectedIconColor,
                    unselectedTextColor = unselectedTextColor,
                    indicatorColor = indicatorColor
                ),
            )
        }
    }
}

data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)
