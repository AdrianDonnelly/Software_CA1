package com.example.carparts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carparts.data.remote.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    innerPadding: PaddingValues,
    onAuthSuccess: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun validateInputs(): Boolean {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password are required."
            return false
        }
        if (!email.contains("@")) {
            errorMessage = "Enter a valid email."
            return false
        }
        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters."
            return false
        }
        return true
    }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Welcome to CarParts",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "Sign in or create an account to continue.",
                    color = Color(0xFF4B5563)
                )

                TextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = Color(0xFFB91C1C))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (!validateInputs()) return@Button
                            isLoading = true
                            scope.launch {
                                AuthRepository.signIn(email.trim(), password)
                                    .onSuccess { onAuthSuccess("Signed in successfully") }
                                    .onFailure { errorMessage = it.message ?: "Sign in failed." }
                                isLoading = false
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Login")
                    }

                    Button(
                        onClick = {
                            if (!validateInputs()) return@Button
                            isLoading = true
                            scope.launch {
                                AuthRepository.signUp(email.trim(), password)
                                    .onSuccess { onAuthSuccess("Account created. You are logged in.") }
                                    .onFailure { errorMessage = it.message ?: "Sign up failed." }
                                isLoading = false
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                    ) {
                        Text("Sign Up")
                    }
                }
            }
        }
    }
}
