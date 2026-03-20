package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen

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
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.R
import kotlinx.coroutines.delay


// ecran de démarrage affiché brièvement au lancement de l'application
@Composable
fun SplashScreen(navController: NavController) {
    val gradientColors = listOf(
        Color(0xFF05001E),
        Color(0xFF1E1165),
        Color(0xFFF299B5)
    )

    // temps avant d'arriver sur l'écran d'accueil
    LaunchedEffect(key1 = true) {
        delay(2500) // on attend 2.5 sec
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }//popUpTo empêche le user d'y revenir avec le bouton retour
        }
    }

    //fond + logo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_app_ronde_cineflix),
            contentDescription = "Logo Rond Cineflix",
            modifier = Modifier.size(250.dp)
        )
    }
}