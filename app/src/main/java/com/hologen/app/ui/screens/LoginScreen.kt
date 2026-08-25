package com.hologen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.hologen.app.ui.theme.HologenColors
import com.hologen.app.ui.theme.HologenMetrics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HologenColors.Background.primary)
            .padding(HologenMetrics.space24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo / App Name
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

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = HologenColors.Text.secondary)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HologenColors.Accent.mint,
                focusedLabelColor = HologenColors.Accent.mint
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = HologenColors.Text.secondary)
            },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HologenColors.Accent.mint,
                focusedLabelColor = HologenColors.Accent.mint
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Login/Register Button
        Button(
            onClick = { if (isLogin) onLoginClick() else onRegisterClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HologenColors.Accent.mint),
            shape = RoundedCornerShape(HologenMetrics.buttonRadius)
        ) {
            Text(
                text = if (isLogin) "Sign In" else "Create Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Toggle Login/Register
        TextButton(onClick = { isLogin = !isLogin }) {
            Text(
                text = if (isLogin) "Don't have an account? Sign Up" else "Already have an account? Sign In",
                color = HologenColors.Accent.mint
            )
        }
    }
}