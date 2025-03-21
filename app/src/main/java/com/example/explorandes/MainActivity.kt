package com.example.explorandes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigator()
        }
    }
}

// Navigation controller
@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000) // Wait 2 seconds
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
    } else {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") { HomeScreen(navController) }
            composable("login") { LoginScreen(navController) }
            composable("main_screen") { MainScreen(navController) }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F29)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Explor", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Andes", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE91E63))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Explore UniAndes Like Never Before", fontSize = 14.sp, color = Color.LightGray)
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F29)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Explor", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Andes", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE91E63))
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("login") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
            ) {
                Text("Start Exploring", color = Color.White)
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F29)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Find Your Way Around",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE91E63)
            )
            Text("Your interactive campus map at a glance", fontSize = 16.sp, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            // Input fields
            TextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Email or Phone Number") },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFF1A1F39),
                    focusedContainerColor = Color(0xFF1A1F39),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Password") },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFF1A1F39),
                    focusedContainerColor = Color(0xFF1A1F39),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("main_screen") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
            ) {
                Text("Sign In", color = Color.White)
            }

            // Skip login button for development
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { navController.navigate("main_screen") }) {
                Text("Skip Login (Development Only)", color = Color.Gray)
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F29)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top greeting
            Text(
                "Welcome to Explorandes",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 32.dp)
            )

            Text(
                "Campus Explorer",
                fontSize = 18.sp,
                color = Color(0xFFE91E63),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Main content area - placeholder for your actual content
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = Color(0xFF1A1F39),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "This is the main content area",
                        fontSize = 18.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "You've successfully bypassed the login screen",
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { navController.navigateUp() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                    ) {
                        Text("Go Back", color = Color.White)
                    }
                }
            }

            // Bottom navigation placeholder
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                color = Color(0xFF1A1F39),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text("Home", color = Color(0xFFE91E63))
                    Text("Explore", color = Color.White)
                    Text("Map", color = Color.White)
                    Text("Profile", color = Color.White)
                }
            }
        }
    }
}