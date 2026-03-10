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
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.theme.CINEFLIX_Karsenti_CoignetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CINEFLIX_Karsenti_CoignetTheme {
                // 1. On crée le contrôleur de navigation unique
                val navController = rememberNavController()

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
                        // 4. On définit nos routes (écrans) dans UN SEUL NavHost
                        NavHost(navController = navController, startDestination = "splash") {
                            composable("splash") { SplashScreen(navController) }
                            composable("auth") { AuthScreen(navController) }
                            composable("home") { HomeScreen(navController) }
                            composable("profile") { ProfileScreen(navController) }

                            // NOUVELLE ROUTE : L'écran de l'univers détaillé
                            composable(
                                route = "universe/{universeName}",
                                arguments = listOf(navArgument("universeName") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val universeName = backStackEntry.arguments?.getString("universeName") ?: ""
                                UniverseScreen(navController = navController, universeName = universeName)
                            }
                        }
                    }
                }
            }
        }
    }
}