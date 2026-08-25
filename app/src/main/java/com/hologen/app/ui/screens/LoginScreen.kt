package com.hologen.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.hologen.app.ui.theme.HologenColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun validateInputs(): Boolean {
        var isValid = true
        
        if (email.isBlank()) {
            emailError = "Email is required"
            isValid = false
        } else if (!isValidEmail(email)) {
            emailError = "Please enter a valid email"
            isValid = false
        } else {
            emailError = null
        }
        
        if (password.isBlank()) {
            passwordError = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordError = null
        }
        
        return isValid
    }
    
    fun handleLogin() {
        errorMessage = null
        
        if (!validateInputs()) return
        
        scope.launch {
            isLoading = true
            delay(1500) // Simulate network delay
            
            // Simple validation for prototype (Replace with real auth later)
            if (email.isNotBlank() && password.length >= 6) {
                onLoginSuccess()
            } else {
                errorMessage = "Invalid credentials. Please try again."
            }
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HologenColors.Background.primary)
    ) {
        // Animated Grid Background
        AnimatedGridBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "HOLOGEN",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = HologenColors.Accent.mint,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Spatial AI Platform",
                style = MaterialTheme.typography.bodyLarge,
                color = HologenColors.Text.secondary,
                modifier = Modifier.padding(bottom = 48.dp)
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    emailError = null
                    errorMessage = null
                },
                label = { Text("Email", color = HologenColors.Text.secondary) },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Email, 
                        contentDescription = null, 
                        tint = if (emailError != null) MaterialTheme.colorScheme.error else HologenColors.Text.secondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HologenColors.Accent.mint,
                    unfocusedBorderColor = HologenColors.Background.cardSecondary,
                    focusedLabelColor = HologenColors.Accent.mint,
                    cursorColor = HologenColors.Accent.mint,
                    errorBorderColor = MaterialTheme.colorScheme.error
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    passwordError = null
                    errorMessage = null
                },
                label = { Text("Password", color = HologenColors.Text.secondary) },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Lock, 
                        contentDescription = null,
                        tint = if (passwordError != null) MaterialTheme.colorScheme.error else HologenColors.Text.secondary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = HologenColors.Text.secondary
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HologenColors.Accent.mint,
                    unfocusedBorderColor = HologenColors.Background.cardSecondary,
                    focusedLabelColor = HologenColors.Accent.mint,
                    cursorColor = HologenColors.Accent.mint,
                    errorBorderColor = MaterialTheme.colorScheme.error
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { handleLogin() }
                ),
                singleLine = true,
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            Button(
                onClick = { handleLogin() },
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HologenColors.Accent.mint,
                    disabledContainerColor = HologenColors.Accent.mint.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = HologenColors.Background.primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = HologenColors.Background.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            TextButton(onClick = { /* TODO: Navigate to Sign Up */ }) {
                Text(
                    text = "Don't have an account? Sign Up",
                    color = HologenColors.Accent.mint,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun AnimatedGridBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        )
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSize = 50f
        val strokeWidth = 1f
        val color = HologenColors.Accent.mint.copy(alpha = 0.1f)
        
        var x = offset % gridSize
        while (x < size.width) {
            drawLine(color = color, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = strokeWidth)
            x += gridSize
        }
        
        var y = offset % gridSize
        while (y < size.height) {
            drawLine(color = color, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = strokeWidth)
            y += gridSize
        }
    }
}