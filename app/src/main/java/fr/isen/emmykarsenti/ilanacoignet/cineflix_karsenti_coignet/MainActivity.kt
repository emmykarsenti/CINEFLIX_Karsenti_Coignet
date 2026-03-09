package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.theme.CINEFLIX_Karsenti_CoignetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CINEFLIX_Karsenti_CoignetTheme(dynamicColor = false) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // On n'affiche la barre que sur Home et Profile
                        if (currentRoute == "home" || currentRoute == "profile") {
                            NavigationBar(containerColor = Color(0xFF1A1D29), contentColor = Color.White) {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.Home, contentDescription = "Accueil") },
                                    label = { Text("Accueil") },
                                    selected = currentRoute == "home",
                                    onClick = { navController.navigate("home") }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profil") },
                                    label = { Text("Profil") },
                                    selected = currentRoute == "profile",
                                    onClick = { navController.navigate("profile") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    // ON FORCE LA TAILLE ET ON MET UN FOND GRIS POUR VOIR SI LE CADRE EXISTE
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color.Gray)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "auth" // <--- ON MET "auth" AU LIEU DE "splash" ICI
                        ) {
                            composable("splash") { SplashScreen(navController) }
                            composable("auth") { AuthScreen(navController) }
                            composable("home") { HomeScreen(navController) }
                            composable("profile") { ProfileScreen(navController) }
                        }
                    }
                }
            }
        }
    }
}