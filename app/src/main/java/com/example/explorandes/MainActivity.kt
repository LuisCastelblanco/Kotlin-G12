package com.example.explorandes

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.explorandes.services.BrightnessController
import com.example.explorandes.services.LightSensorManager
import com.example.explorandes.api.ApiClient
import com.example.explorandes.models.AuthRequest
import com.example.explorandes.models.User
import com.example.explorandes.utils.SessionManager

class MainActivity : ComponentActivity() {

    private lateinit var lightSensorManager: LightSensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar ApiClient con el contexto
        ApiClient.init(applicationContext)
        
        setContent {
            AppNavigator()
        }

        // Initialize light sensor
        lightSensorManager = LightSensorManager(this) { lux ->
            adjustBrightness(lux)
        }

        lightSensorManager.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        lightSensorManager.stopListening()
    }

    private fun adjustBrightness(lux: Float) {
        val brightnessLevel = when {
            lux < 20 -> 1.0f  // Increase brightness in low light
            lux < 100 -> 0.7f
            lux < 500 -> 0.5f
            else -> 0.3f  // Reduce brightness in bright light
        }

        BrightnessController.setBrightness(this, brightnessLevel)
    }
}

// Navigation controller
@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }
    
    // Check if user is already logged in
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    LaunchedEffect(Unit) {
        delay(2000) // Wait 2 seconds
        showSplash = false
        
        // If user is already logged in, navigate directly to HomeActivity
        if (sessionManager.isLoggedIn()) {
            val intent = Intent(context, HomeActivity::class.java)
            context.startActivity(intent)
            if (context is ComponentActivity) {
                context.finish()
            }
        }
    }

    if (showSplash) {
        SplashScreen()
    } else {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") { HomeScreen(navController) }
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
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
                Text("Sign In", color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { navController.navigate("register") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
            ) {
                Text("Create Account", color = Color.White)
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavHostController) {
    // Get the context to launch intent
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Create session manager
    val sessionManager = remember { SessionManager(context) }
    
    // State for input fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // State for loading and error
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F29)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Sign In",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE91E63)
            )
            Text("Find your way around campus", fontSize = 16.sp, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            // Input fields
            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFF1A1F39),
                    focusedContainerColor = Color(0xFF1A1F39),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFF1A1F39),
                    focusedContainerColor = Color(0xFF1A1F39),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                )
            )

            // Display error message if any
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // Validate inputs
                    when {
                        email.isEmpty() -> errorMessage = "Email is required"
                        password.isEmpty() -> errorMessage = "Password is required"
                        else -> {
                            // Clear previous error
                            errorMessage = null
                            isLoading = true
                            
                            // Perform login
                            scope.launch {
                                try {
                                    val response = ApiClient.apiService.login(AuthRequest(email = email, password = password))
                                    // Save token
                                    sessionManager.saveToken(response.token)
                                    
                                    // Navigate to home
                                    val intent = Intent(context, HomeActivity::class.java)
                                    context.startActivity(intent)
                                    // Finish current activity if needed
                                    if (context is ComponentActivity) {
                                        context.finish()
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Login failed: ${e.localizedMessage}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Sign In", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { navController.navigate("register") }
            ) {
                Text("Don't have an account? Sign Up", color = Color(0xFFE91E63))
            }

            // Skip login button for development
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    // Launch HomeActivity
                    val intent = Intent(context, HomeActivity::class.java)
                    context.startActivity(intent)
                    // Finish current activity if needed
                    if (context is ComponentActivity) {
                        context.finish()
                    }
                }
            ) {
                Text("Skip Login (Development Only)", color = Color.Gray)
            }
        }
    }
}

@Composable
fun RegisterScreen(navController: NavHostController) {
    // Get the context to launch intent
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Create session manager
    val sessionManager = remember { SessionManager(context) }
    
    // State for input fields
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // State for loading and error
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F29)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Create Account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3949AB)
            )
            Text("Join the ExplorAndes community", fontSize = 16.sp, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            // Input fields
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFF1A1F39),
                    focusedContainerColor = Color(0xFF1A1F39),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFF1A1F39),
                    focusedContainerColor = Color(0xFF1A1F39),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFF1A1F39),
                    focusedContainerColor = Color(0xFF1A1F39),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFF1A1F39),
                    focusedContainerColor = Color(0xFF1A1F39),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                )
            )

            // Display error message if any
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // Validate inputs
                    when {
                        name.isEmpty() -> errorMessage = "Name is required"
                        email.isEmpty() -> errorMessage = "Email is required"
                        !email.contains("@") -> errorMessage = "Please enter a valid email"
                        password.isEmpty() -> errorMessage = "Password is required"
                        password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                        password != confirmPassword -> errorMessage = "Passwords don't match"
                        else -> {
                            // Clear previous error
                            errorMessage = null
                            isLoading = true
                            
                            // Create user object
                            val newUser = User(
                                username = name, 
                                email = email,
                                password = password,
                                firstName = null,  
                                lastName = null    
                            )
                            
                            // Perform registration
                            scope.launch {
                                try {
                                    val response = ApiClient.apiService.register(newUser)
                                    // Save token from response
                                    sessionManager.saveToken(response.token)
                                    
                                    // Navigate to home
                                    val intent = Intent(context, HomeActivity::class.java)
                                    context.startActivity(intent)
                                    // Finish current activity if needed
                                    if (context is ComponentActivity) {
                                        context.finish()
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Registration failed: ${e.localizedMessage}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Create Account", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { navController.navigate("login") }
            ) {
                Text("Already have an account? Sign In", color = Color(0xFFE91E63))
            }
        }
    }
}