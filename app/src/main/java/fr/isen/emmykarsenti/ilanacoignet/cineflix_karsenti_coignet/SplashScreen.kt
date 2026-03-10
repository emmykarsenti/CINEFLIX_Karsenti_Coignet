package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Le minuteur avant de passer à l'écran d'accueil
    LaunchedEffect(key1 = true) {
        delay(1500) // On réduit à 1.5 seconde pour que ce soit plus rapide
        // On force la direction vers "auth"
        navController.navigate("auth") {
            popUpTo("splash") { inclusive = true }
        }
    }

    // Le fond et le logo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05001E)), // Fond sombre cohérent avec HomeScreen
        contentAlignment = Alignment.Center
    ) {
        Image(
            // Vérifie que c'est bien le nom de ton image ici
            painter = painterResource(id = R.drawable.logo_cineflix),
            contentDescription = "Logo Cineflix",
            modifier = Modifier.size(250.dp)
        )
    }
}
