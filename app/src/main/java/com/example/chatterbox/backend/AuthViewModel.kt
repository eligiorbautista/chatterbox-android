package com.example.chatterbox.backend

import android.app.Activity
import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatterbox.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthViewModel : ViewModel() {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db = Firebase.firestore
    private val storage = FirebaseStorage.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _userProfile = MutableLiveData<Map<String, Any>>()
    val userProfile: LiveData<Map<String, Any>> = _userProfile

    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers: LiveData<List<User>> = _allUsers

    private val _notifications = MutableLiveData<List<NotificationData>>(emptyList())
    val notifications: LiveData<List<NotificationData>> = _notifications

    private lateinit var googleSignInClient: GoogleSignInClient

    private var userListener: ListenerRegistration? = null
    private var usersListener: ListenerRegistration? = null
    private var notificationsListener: ListenerRegistration? = null

    fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
            getUserProfile()
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password cannot be empty.")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        db.collection("users").document(uid).update("isOnline", true)
                            .addOnSuccessListener {
                                _authState.value = AuthState.Authenticated
                                getUserProfile()
                            }
                            .addOnFailureListener {
                                _authState.value = AuthState.Error(it.message ?: "Failed to update online status.")
                            }
                    } else {
                        _authState.value = AuthState.Error("Failed to get user ID.")
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong.")
                }
            }
    }

    fun register(email: String, password: String, displayName: String) {
        if (email.isEmpty() || password.isEmpty() || displayName.isEmpty()) {
            _authState.value = AuthState.Error("Email, password, or display name cannot be empty.")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()

                    val newUser = hashMapOf(
                        "uid" to uid,
                        "displayName" to displayName,
                        "email" to email,
                        "bio" to "",
                        "location" to "",
                        "profilePictureUrl" to "",
                        "isPushNotificationOn" to true,
                        "isTwoFactorAuthenticationOn" to false,
                        "isDarkModeOn" to false,
                        "isOnline" to false
                    )

                    uid?.let {
                        db.collection("users").document(it).set(newUser)
                            .addOnSuccessListener { getUserProfile() }
                            .addOnFailureListener { e: Exception -> Log.w(TAG, "Error adding document", e) }
                    }

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            _authState.value = AuthState.Authenticated
                        } else {
                            _authState.value = AuthState.Error(updateTask.exception?.message ?: "Failed to update profile.")
                        }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Registration failed.")
                }
            }
    }

    fun logout() {
        val uid = getUserId()
        if (uid != null) {
            db.collection("users").document(uid).update("isOnline", false)
                .addOnSuccessListener {
                    auth.signOut()
                    _authState.value = AuthState.Unauthenticated
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating online status", e)
                }
        } else {
            auth.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun updateProfile(displayName: String, bio: String, location: String, profilePictureUri: Uri?) {
        val uid = getUserId() ?: return
        val userProfileUpdates = mutableMapOf<String, Any>(
            "displayName" to displayName,
            "bio" to bio,
            "location" to location
        )

        if (profilePictureUri != null) {
            val storageRef = storage.reference.child("profile_pictures/$uid.jpg")
            val uploadTask: UploadTask = storageRef.putFile(profilePictureUri)

            uploadTask.addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    userProfileUpdates["profilePictureUrl"] = uri.toString()
                    db.collection("users").document(uid).update(userProfileUpdates)
                        .addOnSuccessListener {
                            Log.d(TAG, "UserProfile updated.")
                            getUserProfile() // Refresh the user profile after update
                        }
                        .addOnFailureListener { e: Exception -> Log.w(TAG, "Error updating UserProfile", e) }
                }
            }.addOnFailureListener { e: Exception ->
                Log.w(TAG, "Error uploading profile picture", e)
            }
        } else {
            db.collection("users").document(uid).update(userProfileUpdates)
                .addOnSuccessListener {
                    Log.d(TAG, "UserProfile updated.")
                    getUserProfile() // Refresh the user profile after update
                }
                .addOnFailureListener { e: Exception -> Log.w(TAG, "Error updating UserProfile", e) }
        }

    }

    fun getUserProfile() {
        val uid = getUserId() ?: return
        userListener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Error getting user profile", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    _userProfile.value = snapshot.data
                    Log.d(TAG, "UserProfile: ${snapshot.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
    }

    fun updatePushNotificationSetting(isOn: Boolean) {
        val uid = getUserId() ?: return
        db.collection("users").document(uid).update("isPushNotificationOn", isOn)
            .addOnSuccessListener { Log.d(TAG, "PushNotificationSetting updated.") }
            .addOnFailureListener { e: Exception -> Log.w(TAG, "Error updating PushNotificationSetting", e) }
    }

    fun updateTwoFactorAuthenticationSetting(isOn: Boolean) {
        val uid = getUserId() ?: return
        db.collection("users").document(uid).update("isTwoFactorAuthenticationOn", isOn)
            .addOnSuccessListener { Log.d(TAG, "TwoFactorAuthenticationSetting updated.") }
            .addOnFailureListener { e: Exception -> Log.w(TAG, "Error updating TwoFactorAuthenticationSetting", e) }
    }

    fun updateDarkModeSetting(isOn: Boolean) {
        val uid = getUserId() ?: return
        db.collection("users").document(uid).update("isDarkModeOn", isOn)
            .addOnSuccessListener { Log.d(TAG, "DarkModeSetting updated.") }
            .addOnFailureListener { e: Exception -> Log.w(TAG, "Error updating DarkModeSetting", e) }
    }

    private fun logPasswordReset() {
        val uid = getUserId() ?: return

        val date = Date()

        val timestamp = SimpleDateFormat("MMMM dd, yyyy hh:mma", Locale.getDefault()).format(date)

        val passwordResetLog = hashMapOf(
            "uid" to uid,
            "timestamp" to timestamp,
            "event" to "Account Security",
            "description" to "A request was made to reset your password."
        )

        db.collection("notifications").add(passwordResetLog)
            .addOnSuccessListener { Log.d(TAG, "Password reset logged.") }
            .addOnFailureListener { e: Exception -> Log.w(TAG, "Error logging password reset", e) }
    }


    fun sendPasswordResetEmail(email: String) {
        if (email.isEmpty()) {
            _authState.value = AuthState.Error("Email cannot be empty.")
            return
        }

        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success("Password reset email sent.")
                    logPasswordReset()
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Failed to send password reset email.")
                }
            }
    }

    fun getGoogleSignInClient(activity: Activity): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
        return googleSignInClient
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                if (!document.exists()) {
                                    val newUser = hashMapOf(
                                        "uid" to uid,
                                        "displayName" to (account.displayName ?: ""),
                                        "email" to (account.email ?: ""),
                                        "bio" to "",
                                        "location" to "",
                                        "profilePictureUrl" to (account.photoUrl?.toString() ?: ""),
                                        "isPushNotificationOn" to true,
                                        "isTwoFactorAuthenticationOn" to false,
                                        "isDarkModeOn" to false,
                                        "isOnline" to true
                                    )
                                    db.collection("users").document(uid).set(newUser)
                                        .addOnSuccessListener {
                                            getUserProfile()
                                            _authState.value = AuthState.Authenticated
                                        }
                                        .addOnFailureListener { e: Exception -> Log.w(TAG, "Error adding document", e) }
                                } else {

                                    db.collection("users").document(uid).update("isOnline", true)
                                        .addOnSuccessListener {
                                            getUserProfile()
                                            _authState.value = AuthState.Authenticated
                                        }
                                        .addOnFailureListener { e: Exception -> Log.w(TAG, "Error updating online status", e) }
                                }
                            }
                            .addOnFailureListener { e: Exception -> Log.w(TAG, "Error getting user profile", e) }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Authentication failed.")
                }
            }
    }

    fun updateAuthState(state: AuthState) {
        _authState.value = state
    }

    fun changePassword(currentPassword: String, newPassword: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    onSuccess()
                                    logPasswordChange()
                                } else {
                                    onFailure(updateTask.exception?.message ?: "Password change failed.")
                                }
                            }
                    } else {
                        onFailure(task.exception?.message ?: "Re-authentication failed.")
                    }
                }
        } else {
            onFailure("User is not logged in.")
        }
    }

    private fun logPasswordChange() {
        val uid = getUserId() ?: return

        val date = Date()

        val timestamp = SimpleDateFormat("MMMM dd, yyyy hh:mma", Locale.getDefault()).format(date)

        val passwordChangeLog = hashMapOf(
            "uid" to uid,
            "timestamp" to timestamp,
            "event" to "Account Security",
            "description" to "Your account password was changed."
        )

        db.collection("notifications").add(passwordChangeLog)
            .addOnSuccessListener { Log.d(TAG, "Password change logged.") }
            .addOnFailureListener { e: Exception -> Log.w(TAG, "Error logging password change", e) }
    }


    fun fetchAllUsers() {
        usersListener = db.collection("users").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Error fetching users", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val users = snapshot.mapNotNull { document ->
                    document.data?.let {
                        User(
                            uid = it["uid"] as? String ?: "",
                            displayName = it["displayName"] as? String ?: "",
                            email = it["email"] as? String ?: "",
                            bio = it["bio"] as? String ?: "",
                            location = it["location"] as? String ?: "",
                            profilePictureUrl = it["profilePictureUrl"] as? String ?: "",
                            isPushNotificationOn = it["isPushNotificationOn"] as? Boolean ?: true,
                            isTwoFactorAuthenticationOn = it["isTwoFactorAuthenticationOn"] as? Boolean ?: false,
                            isDarkModeOn = it["isDarkModeOn"] as? Boolean ?: false,
                            isOnline = it["isOnline"] as? Boolean ?: false
                        )
                    }
                }
                _allUsers.value = users
            }
        }
    }

    // Fetch notifications for the logged-in user
    fun fetchUserNotifications() {
        val uid = getUserId() ?: return
        notificationsListener = db.collection("notifications")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Error fetching notifications", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notificationsList = snapshot.mapNotNull { document ->
                        document.toObject(NotificationData::class.java)
                    }
                    _notifications.value = notificationsList
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
        usersListener?.remove()
        notificationsListener?.remove()
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
    data class Success(val message: String) : AuthState()
}

data class User(
    val uid: String,
    val displayName: String,
    val email: String,
    val bio: String,
    val location: String,
    val profilePictureUrl: String,
    val isPushNotificationOn: Boolean,
    val isTwoFactorAuthenticationOn: Boolean,
    val isDarkModeOn: Boolean,
    val isOnline: Boolean
)

data class NotificationData(
    val uid: String = "",
    val timestamp: String = "",
    val event: String = "",
    val description: String = ""
)
