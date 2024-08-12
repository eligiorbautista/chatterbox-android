package com.example.chatterbox

import java.util.regex.Pattern

fun isValidEmail(email: String): Boolean {
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    return Pattern.compile(emailPattern).matcher(email).matches()
}
