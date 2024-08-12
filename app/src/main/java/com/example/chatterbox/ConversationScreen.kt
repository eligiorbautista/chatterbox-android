package com.example.chatterbox

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.chatterbox.backend.AuthViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@SuppressLint("MutableCollectionMutableState")
@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(navController: NavHostController, userName: String, authViewModel : AuthViewModel) {
    val messages = remember { mutableStateListOf<MessageData>(*mockMessages.toTypedArray()) }
    val context = LocalContext.current as Activity
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // State to store the URI of the captured image
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // State to track if the user is typing
    var isTyping by remember { mutableStateOf(false) }

    // State to show/hide image dialog
    var showImageDialog by remember { mutableStateOf(false) }
    var imageUriForDialog by remember { mutableStateOf<Uri?>(null) }

    // State to show/hide gif dialog
    var showGifDialog by remember { mutableStateOf(false) }
    var gifUriForDialog by remember { mutableStateOf<Uri?>(null) }

    val authState by authViewModel.authState.observeAsState()
    val userProfile by authViewModel.userProfile.observeAsState()
    val darkModeEnabled = userProfile?.get("isDarkModeOn") as? Boolean ?: false

    val backgroundColor = if (darkModeEnabled) Color(0xFF2E2E2E) else Color.White
    val textColor = if (darkModeEnabled) Color.White else Color.Black
    val secondaryTextColor = if (darkModeEnabled) Color(0xFF929AAB) else Color.Gray
    val inputFieldColor = if (darkModeEnabled) Color.White else Color.Black
    val buttonColor = if (darkModeEnabled) Color.White else Color(0xFF010101)
    val buttonTextColor = if (darkModeEnabled) Color.Black else Color.White

    // Launchers for picking image and taking picture
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            addImageMessage(messages, it)
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            photoUri?.let {
                addImageMessage(messages, it)
                coroutineScope.launch {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }
        }
    }

    // Permissions launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true

        if (cameraGranted) {
            val photoFile = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
            photoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
            takePictureLauncher.launch(photoUri)
        } else {
            Toast.makeText(context, "Please grant camera permission to take a picture", Toast.LENGTH_SHORT).show()
        }
    }


    // Function to request permissions and take picture
    fun takePicture(context: Activity) {
        val permissions = arrayOf(
            Manifest.permission.CAMERA
        )
        if (permissions.all { ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) {
            val photoFile = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
            photoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
            takePictureLauncher.launch(photoUri)
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }


    // hide the keyboard
    fun hideKeyboard(context: Context) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow((context as Activity).currentFocus?.windowToken, 0)
    }

    // save file to the device
    suspend fun saveImageToGallery(uri: Uri, context: Context, isGif: Boolean) {
        val resolver = context.contentResolver
        val filename = "${System.currentTimeMillis()}.${if (isGif) "gif" else "jpg"}"
        val mimeType = if (isGif) "image/gif" else "image/jpeg"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/ChatterBox/saves")
        }

        val outputUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        outputUri?.let {
            resolver.openOutputStream(it)?.use { stream ->
                if (isGif) {
                    resolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.copyTo(stream)
                    }
                } else {
                    val imageLoader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(uri)
                        .build()
                    val result = (imageLoader.execute(request) as SuccessResult).drawable
                    (result as BitmapDrawable).bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                }
                stream.flush()
                Toast.makeText(context, "Saved to gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.scrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(0.dp),
                title = {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(userName, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Icon(
                                imageVector = Icons.Rounded.Circle,
                                contentDescription = null,
                                tint = Color(0xFF57C75B),
                                modifier = Modifier.size(9.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Online", fontSize = 12.sp, color = Color.Gray)
                        }

                    }
                },
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back")
                        }
                        Image(
                            painter = painterResource(id = R.drawable.default_avatar),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("userProfile/$userName") }) {
                        Icon(Icons.Rounded.Info, contentDescription = "View Profile")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 12.dp, end = 12.dp, bottom = 20.dp)
        ) {

            Divider()

            Spacer(Modifier.height(8.dp))
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Start conversation",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
            // Chat messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 0.dp, bottom = 10.dp, start = 14.dp, end = 14.dp)
            ) {
                items(messages) { message ->
                    when (message) {
                        is TextMessage -> Message(
                            isUser = message.isUser,
                            message = message.content,
                            timestamp = message.timestamp,
                            darkModeEnabled
                        )
                        is ImageMessageData -> ImageMessage(
                            isUser = message.isUser,
                            uri = message.uri,
                            timestamp = message.timestamp,
                            onClick = {
                                imageUriForDialog = message.uri
                                showImageDialog = true
                            }
                        )
                        is GifMessageData -> GifMessage(
                            isUser = message.isUser,
                            uri = message.uri,
                            timestamp = message.timestamp,
                            onClick = {
                                gifUriForDialog = message.uri
                                showGifDialog = true
                            }
                        )
                    }
                }

            }
                }

            Divider()

            var messageText by remember { mutableStateOf(TextFieldValue("")) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .background(Color.LightGray.copy(alpha = 0.2f), shape = RoundedCornerShape(30.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = messageText,
                    onValueChange = { text ->
                        messageText = text
                        isTyping = text.text.isNotBlank()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    singleLine = false,
                    decorationBox = { innerTextField ->
                        if (messageText.text.isEmpty()) {
                            Text("Message...", color = Color.Gray)
                        }
                        innerTextField()
                    }
                )
                if (isTyping) {
                    IconButton(onClick = {
                        messageText = TextFieldValue("")
                        isTyping = false
                        hideKeyboard(context)
                    }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                } else {
                    IconButton(onClick = { pickImageLauncher.launch("image/*") }) {
                        Icon(Icons.Rounded.Image, modifier = Modifier.size(22.dp), contentDescription = "Send Image")
                    }
                    IconButton(onClick = {
                        takePicture(context)
                    }) {
                        Icon(Icons.Rounded.CameraAlt, modifier = Modifier.size(22.dp), contentDescription = "Take Picture")
                    }
                }
                IconButton(
                    onClick = {
                        if (messageText.text.isNotBlank()) {
                            addTextMessage(messages, messageText.text)
                            messageText = TextFieldValue("")
                            isTyping = false
                            coroutineScope.launch {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                            hideKeyboard(context)
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, modifier = Modifier.size(22.dp), contentDescription = "Send Message")
                }
            }
        }
    }

    // Image dialog
    if (showImageDialog && imageUriForDialog != null) {
        Dialog(
            onDismissRequest = { showImageDialog = false }
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                ZoomableImage(imageUriForDialog!!)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = { coroutineScope.launch { saveImageToGallery(imageUriForDialog!!, context, false) } }
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = "Download", tint = Color.White)
                    }
                    IconButton(
                        onClick = { showImageDialog = false }
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }
    }

    // Gif dialog
    if (showGifDialog && gifUriForDialog != null) {
        Dialog(
            onDismissRequest = { showGifDialog = false }
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                ZoomableGif(gifUriForDialog!!)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = { coroutineScope.launch { saveImageToGallery(gifUriForDialog!!, context, true) } }
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = "Download", tint = Color.White)
                    }
                    IconButton(
                        onClick = { showGifDialog = false }
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun Message(isUser: Boolean, message: String, timestamp: String, darkModeEnabled : Boolean) {
    val backgroundColor =
        if (darkModeEnabled) {
            if (isUser) Color(0xFF353535) else Color(0xFF707070)
        }
        else {
            if (isUser) Color(0xFF353535) else Color(0xFFD6D6D6)
        }
    val textColor =
        if (darkModeEnabled) {
            if (isUser) Color(0xFFFFFFFF) else Color(0xFFFFFFFF)
        }
        else {
            if (isUser) Color(0xFFFFFFFF) else Color(0xFF000000)
        }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)

    ) {
        Box(
            modifier = Modifier
                .align(if (isUser) Alignment.End else Alignment.Start)
                .background(backgroundColor, shape = MaterialTheme.shapes.large)
                .padding(12.dp)
        ) {
            Text(
                modifier = Modifier.clickable {
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(message))
                    Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
                },
                text = message, color = textColor)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = timestamp,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.align(if (isUser) Alignment.End else Alignment.Start)
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ImageMessage(isUser: Boolean, uri: Uri, timestamp: String, onClick: () -> Unit) {
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .align(alignment)
                .clickable { onClick() }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .decoderFactory(ImageDecoderDecoder.Factory())
                    .build(),
                contentDescription = "Sent Image",
                modifier = Modifier
                    .padding(8.dp)
                    .sizeIn(maxWidth = 250.dp, maxHeight = 250.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = timestamp,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.align(alignment)
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun GifMessage(isUser: Boolean, uri: Uri, timestamp: String, onClick: () -> Unit) {
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .align(alignment)
                .clickable { onClick() }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .decoderFactory(ImageDecoderDecoder.Factory())
                    .build(),
                contentDescription = "Sent GIF",
                modifier = Modifier
                    .padding(8.dp)
                    .sizeIn(maxWidth = 250.dp, maxHeight = 250.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = timestamp,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.align(alignment)
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ZoomableImage(uri: Uri) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale *= zoom
                    offsetX += pan.x
                    offsetY += pan.y
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .decoderFactory(ImageDecoderDecoder.Factory())
                .build(),
            contentDescription = "Zoomable Image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = maxOf(1f, minOf(3f, scale)),
                    scaleY = maxOf(1f, minOf(3f, scale)),
                    translationX = offsetX,
                    translationY = offsetY
                )
        )
    }
}



@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ZoomableGif(uri: Uri) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale *= zoom
                    offsetX += pan.x
                    offsetY += pan.y
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .decoderFactory(ImageDecoderDecoder.Factory())
                .build(),
            contentDescription = "Zoomable GIF",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = maxOf(1f, minOf(3f, scale)),
                    scaleY = maxOf(1f, minOf(3f, scale)),
                    translationX = offsetX,
                    translationY = offsetY
                )
        )
    }


}

// Data classes for messages
sealed class MessageData
data class TextMessage(val isUser: Boolean, val content: String, val timestamp: String) : MessageData()
data class ImageMessageData(val isUser: Boolean, val uri: Uri, val timestamp: String) : MessageData()
data class GifMessageData(val isUser: Boolean, val uri: Uri, val timestamp: String) : MessageData()

// Messages
val mockMessages = mutableListOf<MessageData>(
    TextMessage(isUser = false, content = "What is your favorite programming language?", timestamp = "10:00 AM"),
    TextMessage(isUser = true, content = "Guess what?", timestamp = "10:02 AM"),
    TextMessage(isUser = false, content = "What?", timestamp = "10:03 AM"),
    ImageMessageData(isUser = true, uri = Uri.parse("android.resource://com.example.chatterbox/drawable/js"), timestamp = "10:05 AM"),
    TextMessage(isUser = false, content = "JavaScript is my favorite too! 😍", timestamp = "10:06 AM"),
    TextMessage(isUser = false, content = "I love how versatile it is. You can use it for both frontend and backend development.", timestamp = "10:07 AM"),
    TextMessage(isUser = true, content = "Absolutely! I've been using it for all my recent projects.", timestamp = "10:08 AM"),
    TextMessage(isUser = false, content = "What frameworks have you been working with?", timestamp = "10:09 AM"),
    TextMessage(isUser = true, content = "Mostly React for frontend and Node.js for backend. How about you?", timestamp = "10:10 AM"),
    ImageMessageData(isUser = false, uri = Uri.parse("android.resource://com.example.chatterbox/drawable/react"), timestamp = "10:11 AM"),
    TextMessage(isUser = true, content = "Same here! React is amazing.", timestamp = "10:12 AM"),
    GifMessageData(isUser = false, uri = Uri.parse("android.resource://com.example.chatterbox/drawable/ohyeah"), timestamp = "10:13 AM"),
    GifMessageData(isUser = true, uri = Uri.parse("android.resource://com.example.chatterbox/drawable/yobro"), timestamp = "10:14 AM")
)

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")

// add a text message
fun addTextMessage(messages: SnapshotStateList<MessageData>, content: String) {
    val timestamp = LocalTime.now().format(timeFormatter)
    messages.add(TextMessage(isUser = true, content = content, timestamp = timestamp))

}

// add an image message
fun addImageMessage(messages: SnapshotStateList<MessageData>, uri: Uri) {
    val timestamp = LocalTime.now().format(timeFormatter)
    messages.add(ImageMessageData(isUser = true, uri = uri, timestamp = timestamp))
}

// add a GIF message
fun addGifMessage(messages: SnapshotStateList<MessageData>, uri: Uri) {
    val timestamp = LocalTime.now().format(timeFormatter)
    messages.add(GifMessageData(isUser = true, uri = uri, timestamp = timestamp))
}
