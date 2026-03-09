package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.Color
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.theme.CINEFLIX_Karsenti_CoignetTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CINEFLIX_Karsenti_CoignetTheme {
                // 1. On crée le contrôleur de navigation
                val navController = rememberNavController()
                // On dit à l'application de commencer par la route "splash"
                NavHost(navController = navController, startDestination = "splash") {

                    // Notre fameux écran de chargement
                    composable("splash") {
                        SplashScreen(navController = navController)
                    }

                    // Ton écran principal (celui avec les cases)
                    composable("home") {
                        HomeScreen(navController = navController) // Remplace HomeScreen par le nom exact de ta page d'accueil si c'est différent
                    }
                }
                // 2. On récupère la route actuelle pour savoir sur quel écran on est
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    // 3. On ajoute la barre du bas conditionnellement
                    bottomBar = {
                        // On n'affiche la barre que si on est sur 'home' ou 'profile'
                        if (currentRoute == "home" || currentRoute == "profile") {
                            NavigationBar(containerColor = Color(0xFF1A1D29), contentColor = Color.White) {

                                // Bouton Accueil
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.Home, contentDescription = "Accueil") },
                                    label = { Text("Accueil") },
                                    selected = currentRoute == "home",
                                    onClick = {
                                        navController.navigate("home") {
                                            // Évite d'empiler les écrans à l'infini quand on clique plusieurs fois
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                // Bouton Profil
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profil") },
                                    label = { Text("Profil") },
                                    selected = currentRoute == "profile",
                                    onClick = {
                                        navController.navigate("profile") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        // 4. On définit nos routes (écrans)
                        NavHost(navController = navController, startDestination = "auth") {
                            composable("auth") { AuthScreen(navController) }
                            composable("home") { HomeScreen() }
                            composable("profile") { ProfileScreen(navController) }
                        }
                    }
                }
            }
        }
    }
}