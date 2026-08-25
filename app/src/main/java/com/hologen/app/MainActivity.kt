package com.hologen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.google.firebase.auth.FirebaseAuth
import com.hologen.app.ui.navigation.HologenNavigation
import com.hologen.app.ui.navigation.HologenTab
import com.hologen.app.ui.screens.LoginScreen
import com.hologen.app.ui.screens.ScanScreen
import com.hologen.app.ui.screens.SettingsScreen
import com.hologen.app.ui.screens.WorkspaceScreen
import com.hologen.app.ui.theme.HologenTheme

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        
        setContent {
            HologenTheme {
                MainApp()
            }
        }
    }

    @Composable
    fun MainApp() {
        var selectedTab by remember { mutableStateOf(HologenTab.Scan) }
        var isLoggedIn by rememberSaveable { mutableStateOf(auth.currentUser != null) }

        LaunchedEffect(Unit) {
            auth.addAuthStateListener { firebaseAuth ->
                isLoggedIn = firebaseAuth.currentUser != null
            }
        }

        if (!isLoggedIn) {
            LoginScreen(
                onLoginSuccess = { 
                    auth.signInWithEmailAndPassword("test@test.com", "password123")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                isLoggedIn = true
                            }
                        }
                },
                onForgotPassword = { email ->
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                            }
                        }
                }
            )
        } else {
            HologenNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            ) { modifier ->
                when (selectedTab) {
                    HologenTab.Scan -> ScanScreen(modifier = modifier)
                    HologenTab.Workspace -> WorkspaceScreen(modifier = modifier)
                    HologenTab.Settings -> SettingsScreen(modifier = modifier)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}