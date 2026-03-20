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

        // affichage plein écran sous la barre de statut Android
        enableEdgeToEdge()

        setContent {
            CINEFLIX_Karsenti_CoignetTheme {

                // navController gère la navigation entre les écrans
                val navController = rememberNavController()

                // on observe la route actuelle pour savoir quel onglet surligner dans la bottom bar
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // le scaffold qui pose la structure de base : fond + bottom bar + contenu principal
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        //bottom bar cachée sur l'écran de connexion
                        if (currentRoute != "auth") {
                            NavigationBar(
                                containerColor = Color(0xFF1A1D29), // Couleur de fond (Bleu/Gris très foncé)
                                contentColor = Color.White // Couleur des icônes
                            ) {
                                // bouton accueil
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.Home, contentDescription = "Accueil") },
                                    label = { Text("Accueil") },
                                    selected = currentRoute == "home", // Surligné si on est sur "home"
                                    onClick = { navController.navigate("home") }
                                )
                                // bouton échange/market
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Échanges") },
                                    label = { Text("Échanges") },
                                    selected = currentRoute == "market",
                                    onClick = { navController.navigate("market") }
                                )
                                // bouton profil
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

                    //le navhost fait le lien entre chaque route (string) et l'écran correspondant
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = "auth" //l'app démarre toujours sur la page de connexion
                        ) {

                            // routes sans paramètres
                            composable("auth") { AuthScreen(navController) }
                            composable("home") { HomeScreen(navController) }
                            composable("market") { MarketScreen(navController) }
                            composable("profile") { ProfileScreen(navController) }

                            //pour univers on passe le nom de l'univers sélectionné (disney, marvel, etc...)
                            composable("universe/{universeName}") {
                                val universeName = it.arguments?.getString("universeName") ?: ""
                                UniverseScreen(
                                    navController = navController,
                                    universeName = universeName
                                )
                            }

                            // détail d'un film où on passe titre, année, genre, durée, réalisateur et franchise
                            composable("movie/{titre}/{annee}/{genre}/{duree}/{realisateur}/{franchise}") {
                                val titre = it.arguments?.getString("titre") ?: "Titre inconnu"
                                val annee = it.arguments?.getString("annee") ?: "Année inconnue"
                                val genre = it.arguments?.getString("genre") ?: "Genre inconnu"
                                val duree = it.arguments?.getString("duree") ?: "Durée inconnue"
                                val realisateur = it.arguments?.getString("realisateur") ?: "Réalisateur inconnu"
                                val franchise = it.arguments?.getString("franchise") ?: "Franchise inconnue"

                                //affichage de l'écran de détails en lui donnant ces informations
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

                            //pour genre on passe le nom et l'id du genre pour charger les films correspondants
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