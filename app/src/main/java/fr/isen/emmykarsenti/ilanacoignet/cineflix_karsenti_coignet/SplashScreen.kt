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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // 1. Les couleurs de ton dégradé : Bleu très foncé vers le Rose du "F" et "x"
    val gradientColors = listOf(
        Color(0xFF05001E), // Bleu nuit
        Color(0xFF1E1165), // Bleu moyen
        Color(0xFFF299B5)  // Rose Cineflix
    )

    // 2. Le minuteur avant de passer à l'écran d'accueil
    LaunchedEffect(key1 = true) {
        delay(2500) // Attend 2.5 secondes
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    // 3. Le fond dégradé et le logo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            // Vérifie que c'est bien le nom de la bonne image ici
            painter = painterResource(id = R.drawable.logo_app_ronde_cineflix),
            contentDescription = "Logo Rond Cineflix",
            modifier = Modifier.size(250.dp)
        )
    }
}