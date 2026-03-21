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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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

        enableEdgeToEdge()

        setContent {
            CINEFLIX_Karsenti_CoignetTheme {

                val navController = rememberNavController() //gère la navigation entre les écrans
                val ongletActif = remember { mutableStateOf("auth") } // on suit manuellement l'onglet actif pour gérer le surlignage et la visibilité de la bottom bar

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { // la bottom bar est cachée sur l'écran de connexion
                        if (ongletActif.value != "auth") {
                            NavigationBar(
                                containerColor = Color(0xFF1A1D29),
                                contentColor = Color.White
                            ) {
                                // bouton accueil
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.Home, contentDescription = "Accueil") },
                                    label = { Text("Accueil") },
                                    selected = ongletActif.value == "home", // surligné si l'onglet actif est "home"
                                    onClick = {
                                        navController.navigate("home")
                                        ongletActif.value = "home"
                                    }
                                )
                                // bouton échanges
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Échanges") },
                                    label = { Text("Échanges") },
                                    selected = ongletActif.value == "market",
                                    onClick = {
                                        navController.navigate("market")
                                        ongletActif.value = "market"
                                    }
                                )
                                // bouton profil
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profil") },
                                    label = { Text("Profil") },
                                    selected = ongletActif.value == "profile",
                                    onClick = {
                                        navController.navigate("profile")
                                        ongletActif.value = "profile"
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    // le navhost fait le lien entre chaque route (string) et l'écran correspondant
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = "auth" //l'app démarre toujours sur la page de connexion
                        ) {
                            // routes sans paramètres
                            composable("auth") {
                                ongletActif.value = "auth"
                                AuthScreen(navController)
                            }
                            composable("home") {
                                ongletActif.value = "home"
                                HomeScreen(navController)
                            }
                            composable("market") {
                                ongletActif.value = "market"
                                MarketScreen(navController)
                            }
                            composable("profile") {
                                ongletActif.value = "profile"
                                ProfileScreen(navController)
                            }
                            // univers : on passe le nom de l'univers sélectionné (disney, marvel, etc.)
                            composable("universe/{universeName}") {
                                val universeName = it.arguments?.getString("universeName") ?: ""
                                UniverseScreen(navController = navController, universeName = universeName)
                            }
                            // détail d'un film : on passe titre, année, genre, durée, réalisateur et franchise
                            composable("movie/{titre}/{annee}/{genre}/{duree}/{realisateur}/{franchise}") {
                                val titre = it.arguments?.getString("titre") ?: "Titre inconnu"
                                val annee = it.arguments?.getString("annee") ?: "Année inconnue"
                                val genre = it.arguments?.getString("genre") ?: "Genre inconnu"
                                val duree = it.arguments?.getString("duree") ?: "Durée inconnue"
                                val realisateur = it.arguments?.getString("realisateur") ?: "Réalisateur inconnu"
                                val franchise = it.arguments?.getString("franchise") ?: "Franchise inconnue"
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
                            // genre : on passe le nom et l'id du genre pour charger les films correspondants
                            composable("genre/{genreName}/{genreId}") {
                                val genreName = it.arguments?.getString("genreName") ?: "Inconnu"
                                val genreId = it.arguments?.getString("genreId") ?: "0"
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