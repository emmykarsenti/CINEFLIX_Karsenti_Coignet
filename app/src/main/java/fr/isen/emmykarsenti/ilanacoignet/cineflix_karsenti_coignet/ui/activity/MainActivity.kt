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
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.GenreScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.HomeScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.MarketScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.MovieDetailScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.ProfileScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen.UniverseScreen
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.theme.CINEFLIX_Karsenti_CoignetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permet à l'application de s'afficher en plein écran (sous la barre de statut et de navigation d'Android)
        enableEdgeToEdge()

        setContent {
            // Application de notre thème personnalisé (couleurs, polices, etc.)
            CINEFLIX_Karsenti_CoignetTheme {

                // 1. INITIALISATION DE LA NAVIGATION
                // navController est le "volant" de notre application, il permet de passer d'un écran à l'autre
                val navController = rememberNavController()

                // On observe l'écran actuel pour savoir quel onglet de la barre de navigation doit être surligné
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // 2. STRUCTURE PRINCIPALE DE L'ÉCRAN (Scaffold)
                // Scaffold est un composant de base de Material Design qui facilite le placement des barres d'outils et de navigation
                Scaffold(
                    modifier = Modifier.fillMaxSize(),

                    // BARRE DE NAVIGATION EN BAS (BottomBar)
                    bottomBar = {
                        // On cache la barre de navigation si on est sur l'écran de connexion (auth)
                        if (currentRoute != "auth") {
                            NavigationBar(
                                containerColor = Color(0xFF1A1D29), // Couleur de fond (Bleu/Gris très foncé)
                                contentColor = Color.White // Couleur des icônes
                            ) {
                                // Bouton : Accueil
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.Home, contentDescription = "Accueil") },
                                    label = { Text("Accueil") },
                                    selected = currentRoute == "home", // Surligné si on est sur "home"
                                    onClick = { navController.navigate("home") }
                                )
                                // Bouton : Échanges (Market)
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Échanges") },
                                    label = { Text("Échanges") },
                                    selected = currentRoute == "market",
                                    onClick = { navController.navigate("market") }
                                )
                                // Bouton : Profil
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

                    // 3. GESTIONNAIRE DES ROUTES (NavHost)
                    // C'est ici qu'on fait le lien entre une "URL" (String) et l'écran (Composable) à afficher.
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = "auth" // L'écran de départ au lancement de l'application
                        ) {

                            // ROUTES SIMPLES (Sans paramètres)
                            composable("auth") { AuthScreen(navController) }
                            composable("home") { HomeScreen(navController) }
                            composable("market") { MarketScreen(navController) }
                            composable("profile") { ProfileScreen(navController) }

                            // ROUTE DE L'UNIVERS (Avec 1 paramètre)
                            composable("universe/{universeName}") {
                                val universeName = it.arguments?.getString("universeName") ?: ""
                                UniverseScreen(
                                    navController = navController,
                                    universeName = universeName
                                )
                            }

                            // ROUTE DES DÉTAILS DU FILM (Avec 6 paramètres)
                            composable("movie/{titre}/{annee}/{genre}/{duree}/{realisateur}/{franchise}") {
                                // On utilise "it" pour accéder directement aux arguments de l'URL
                                val titre = it.arguments?.getString("titre") ?: "Titre inconnu"
                                val annee = it.arguments?.getString("annee") ?: "Année inconnue"
                                val genre = it.arguments?.getString("genre") ?: "Genre inconnu"
                                val duree = it.arguments?.getString("duree") ?: "Durée inconnue"
                                val realisateur = it.arguments?.getString("realisateur") ?: "Réalisateur inconnu"
                                val franchise = it.arguments?.getString("franchise") ?: "Franchise inconnue"

                                // On affiche l'écran de détails en lui donnant ces informations
                                MovieDetailScreen(
                                    navController = navController,
                                    titre = titre,
                                    annee = annee,
                                    genre = genre,
                                    duree = duree,
                                    realisateur = realisateur,
                                    franchise = franchise
                                )
                            }

                            // ROUTE DES GENRES (Avec 2 paramètres)
                            composable("genre/{genreName}/{genreId}") {
                                val genreName = it.arguments?.getString("genreName") ?: "Inconnu"
                                val genreId = it.arguments?.getString("genreId") ?: "0"

                                // Appel direct de l'écran (sans stocker dans une variable)
                                GenreScreen(
                                    navController = navController,
                                    genreName = genreName,
                                    genreId = genreId
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}