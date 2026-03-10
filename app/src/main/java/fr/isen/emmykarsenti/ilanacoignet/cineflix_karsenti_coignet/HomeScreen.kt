package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

// Petite structure de données pour relier un nom d'univers à son logo
data class Universe(val name: String, val imageUrl: String)

@Composable
fun HomeScreen(navController: NavController? = null) {
    val backgroundDark = Color(0xFF1A1D29) // Fond bleu très foncé (style Disney+)
    val cardBackground = Color(0xFF31343E) // Gris foncé pour les cases

    // On utilise des images générées avec un fond clair pour être SÛRS de les voir
    val universes = listOf(
        Universe("Disney", "https://placehold.co/400x200/003366/FFFFFF/png?text=DISNEY"),
        Universe("Pixar", "https://placehold.co/400x200/808080/FFFFFF/png?text=PIXAR"),
        Universe("Marvel", "https://placehold.co/400x200/E23636/FFFFFF/png?text=MARVEL"),
        Universe("Star Wars", "https://placehold.co/400x200/000000/FFE81F/png?text=STAR+WARS"),
        Universe("Avatar", "https://placehold.co/400x200/00A8FF/FFFFFF/png?text=AVATAR")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // En-tête avec le nom de l'app
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "CINEFLIX",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // La fameuse grille avec les logos !
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2 colonnes
            horizontalArrangement = Arrangement.spacedBy(16.dp), // Espace horizontal
            verticalArrangement = Arrangement.spacedBy(16.dp),   // Espace vertical
            modifier = Modifier.fillMaxWidth()
        ) {
            items(universes) { universe ->
                Card(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth()
                        .clickable {
                            // On navigue vers l'écran de l'univers en passant son nom !
                            navController?.navigate("universe/${universe.name}")
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AsyncImage(
                            model = universe.imageUrl,
                            contentDescription = "Logo ${universe.name}",
                            modifier = Modifier
                                .padding(16.dp) // Un peu de marge interne pour que le logo respire
                                .fillMaxSize(),
                            contentScale = ContentScale.Fit // Pour que le logo rentre bien sans être déformé
                        )
                    }
                }
            }
        }
    }
}