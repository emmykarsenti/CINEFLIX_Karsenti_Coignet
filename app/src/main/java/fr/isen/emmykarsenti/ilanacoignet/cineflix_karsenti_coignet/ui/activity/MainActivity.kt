/* package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.activity

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
import androidx.compose.material.icons.filled.ShoppingCart
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
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.AuthScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.HomeScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.MarketScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.MovieDetailScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.ProfileScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.UniverseScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.theme.CINEFLIX_Karsenti_CoignetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CINEFLIX_Karsenti_CoignetTheme {

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute != "auth") {
                            NavigationBar(
                                containerColor = Color(0xFF1A1D29),
                                contentColor = Color.White
                            ) {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.Home, contentDescription = "Accueil") },
                                    label = { Text("Accueil") },
                                    selected = currentRoute == "home",
                                    onClick = { navController.navigate("home") }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Échanges") },
                                    label = { Text("Échanges") },
                                    selected = currentRoute == "market",
                                    onClick = { navController.navigate("market") }
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
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(navController = navController, startDestination = "auth") {

                            composable("auth") { AuthScreen(navController) }
                            composable("home") { HomeScreen(navController) }
                            composable("market") { MarketScreen(navController) }
                            composable("profile") { ProfileScreen(navController) }

                            composable("universe/{universeName}") {
                                val universeName = it.arguments?.getString("universeName") ?: ""
                                UniverseScreen(
                                    navController = navController,
                                    universeName = universeName
                                )
                            }

                            composable("movie/{titre}/{annee}/{genre}/{duree}/{realisateur}") { backStackEntry ->
                                // On récupère les informations de l'URL de navigation
                                val titre = backStackEntry.arguments?.getString("titre") ?: ""
                                val annee = backStackEntry.arguments?.getString("annee") ?: ""
                                val genre = backStackEntry.arguments?.getString("genre") ?: ""
                                val duree = backStackEntry.arguments?.getString("duree") ?: "Inconnue"
                                val realisateur = backStackEntry.arguments?.getString("realisateur") ?: "Inconnu"
                                MovieDetailScreen(
                                    navController = navController,
                                    titre = titre,
                                    annee = annee,
                                    genre = genre,
                                    duree = duree,
                                    realisateur = realisateur
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}*/

package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.activity

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
import androidx.compose.material.icons.filled.ShoppingCart
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
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.AuthScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.HomeScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.MarketScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.MovieDetailScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.ProfileScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.UniverseScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.theme.CINEFLIX_Karsenti_CoignetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CINEFLIX_Karsenti_CoignetTheme {

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute != "auth") {
                            NavigationBar(
                                containerColor = Color(0xFF1A1D29),
                                contentColor = Color.White
                            ) {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.Home, contentDescription = "Accueil") },
                                    label = { Text("Accueil") },
                                    selected = currentRoute == "home",
                                    onClick = { navController.navigate("home") }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Échanges") },
                                    label = { Text("Échanges") },
                                    selected = currentRoute == "market",
                                    onClick = { navController.navigate("market") }
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
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(navController = navController, startDestination = "auth") {

                            composable("auth") { AuthScreen(navController) }
                            composable("home") { HomeScreen(navController) }
                            composable("market") { MarketScreen(navController) }
                            composable("profile") { ProfileScreen(navController) }

                            composable("universe/{universeName}") {
                                val universeName = it.arguments?.getString("universeName") ?: ""
                                UniverseScreen(
                                    navController = navController,
                                    universeName = universeName
                                )
                            }

                            composable("movie/{movieTitre}/{movieAnnee}/{movieGenre}") {
                                val titre = (it.arguments?.getString("movieTitre") ?: "").replace('_', ' ')
                                val annee = it.arguments?.getString("movieAnnee") ?: ""
                                val genre = it.arguments?.getString("movieGenre") ?: ""
                                //val duree = it.arguments?.getString("movieDuree") ?: ""
                                //val realisateur = (it.arguments?.getString("movieRealisateur") ?: "").replace('_', ' ')
                                MovieDetailScreen(
                                    navController = navController,
                                    titre = titre,
                                    annee = annee,
                                    genre = genre,
                                    //duree = duree,
                                    //realisateur = realisateur
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}