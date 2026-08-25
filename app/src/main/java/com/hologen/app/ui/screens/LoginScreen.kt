package com.hologen.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val BgColor = Color(0xFF050505)
private val PrimaryText = Color(0xFFFFFFFF)
private val SecondaryText = Color(0xFF888888)
private val BorderColor = Color(0xFF333333)
private val ButtonBg = Color(0xFFFFFFFF)
private val ButtonText = Color(0xFF000000)
private val ErrorColor = Color(0xFFFF5252)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onForgotPassword: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun handleLogin() {
        errorMessage = null
        if (email.isBlank()) {
            errorMessage = "Email is required"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage = "Invalid email format"
            return
        }
        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters"
            return
        }
        
        scope.launch {
            isLoading = true
            delay(1500)
            onLoginSuccess()
            isLoading = false
        }
    }

    if (showForgotPassword) {
        ForgotPasswordDialog(
            email = resetEmail,
            onEmailChange = { resetEmail = it },
            onDismiss = { showForgotPassword = false },
            onSendResetLink = {
                if (resetEmail.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
                    onForgotPassword(resetEmail)
                    Toast.makeText(context, "Password reset link sent!", Toast.LENGTH_LONG).show()
                    showForgotPassword = false
                    resetEmail = ""
                } else {
                    Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        SubtleGridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "HOLOGEN",
                    color = PrimaryText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Icon(
                    Icons.Outlined.Cube,
                    contentDescription = null,
                    tint = PrimaryText,
                    modifier = Modifier.size(16.dp).padding(start = 4.dp, bottom = 8.dp)
                )
            }
            Text(
                text = "Spatial AI Platform",
                color = SecondaryText,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    errorMessage = null
                },
                label = { Text("Email", color = SecondaryText) },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = SecondaryText) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (errorMessage != null) ErrorColor else PrimaryText,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor = PrimaryText,
                    unfocusedLabelColor = SecondaryText,
                    cursorColor = PrimaryText,
                    textColor = PrimaryText
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                isError = errorMessage != null && email.isBlank()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    errorMessage = null
                },
                label = { Text("Password", color = SecondaryText) },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = SecondaryText) },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = null,
                            tint = SecondaryText
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (errorMessage != null) ErrorColor else PrimaryText,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor = PrimaryText,
                    unfocusedLabelColor = SecondaryText,
                    cursorColor = PrimaryText,
                    textColor = PrimaryText
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { handleLogin() }),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                isError = errorMessage != null && password.isBlank()
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = ErrorColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { handleLogin() },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBg,
                    contentColor = ButtonText,
                    disabledContainerColor = ButtonBg.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ButtonText,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { showForgotPassword = true }) {
                Text(
                    text = "Forgot Password?",
                    color = SecondaryText,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor, thickness = 1.dp)
                Text(
                    text = " Or continue with ",
                    color = SecondaryText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor, thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                SocialButton(label = "G")
                SocialButton(label = "A")
            }

            Row {
                Text(
                    text = "Don't have an account? ",
                    color = SecondaryText,
                    fontSize = 14.sp
                )
                Text(
                    text = "Sign up",
                    color = PrimaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { }
                )
            }
        }
    }
}

@Composable
private fun SocialButton(label: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .border(1.dp, BorderColor, CircleShape)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = PrimaryText,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun ForgotPasswordDialog(
    email: String,
    onEmailChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSendResetLink: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Reset Password",
                color = PrimaryText
            ) 
        },
        text = {
            Column {
                Text(
                    text = "Enter your email address and we'll send you a link to reset your password.",
                    color = SecondaryText,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email", color = SecondaryText) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryText,
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = PrimaryText,
                        unfocusedLabelColor = SecondaryText,
                        cursorColor = PrimaryText,
                        textColor = PrimaryText
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSendResetLink) {
                Text("Send Link", color = PrimaryText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SecondaryText)
            }
        },
        containerColor = BgColor
    )
}

@Composable
private fun SubtleGridBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val color = Color.White.copy(alpha = 0.03f)
        val strokeWidth = 1f
        val width = size.width
        val height = size.height
        
        for (i in 0..10) {
            val y = height * (i / 10f)
            drawLine(
                color = color,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = strokeWidth
            )
        }
        
        for (i in 0..10) {
            val x = width * (i / 10f)
            drawLine(
                color = color,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = strokeWidth
            )
        }
    }
}