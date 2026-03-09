package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
// Voici les imports de Navigation ajoutés tout en haut !
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.theme.CINEFLIX_Karsenti_CoignetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permet à l'application de prendre tout l'écran (derrière la barre de statut)
        enableEdgeToEdge()

        setContent {
            CINEFLIX_Karsenti_CoignetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Le Box permet d'appliquer le "innerPadding" pour que ton formulaire
                    // ne soit pas caché par l'encoche de la caméra ou la barre du bas
                    Box(modifier = Modifier.padding(innerPadding)) {

                        // 1. On crée le contrôleur de navigation
                        val navController = rememberNavController()

                        // 2. On définit nos routes (écrans)
                        NavHost(navController = navController, startDestination = "auth") {
                            // Écran de connexion
                            composable("auth") { AuthScreen(navController) }

                            // Écran d'accueil (la liste des univers)
                            composable("home") { HomeScreen() }
                        }
                    }
                }
            }
        }
    }
}