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
// L'import de la fameuse bibliothèque Coil !
import coil.compose.AsyncImage

// Petite structure de données pour relier un nom d'univers à son logo
data class Universe(val name: String, val imageUrl: String)

@Composable
fun HomeScreen(navController: NavController? = null) {
    val backgroundDark = Color(0xFF1A1D29) // Fond bleu très foncé (style Disney+)
    val cardBackground = Color(0xFF31343E) // Gris foncé pour les cases

    // Liste des univers avec le proxy d'image pour contourner le blocage de Wikipédia
    val universes = listOf(
        Universe("Disney", "https://wsrv.nl/?url=https://upload.wikimedia.org/wikipedia/commons/thumb/a/a4/Disney_wordmark.svg/512px-Disney_wordmark.svg.png"),
        Universe("Pixar", "https://wsrv.nl/?url=https://upload.wikimedia.org/wikipedia/commons/thumb/4/40/Pixar_logo.svg/512px-Pixar_logo.svg.png"),
        Universe("Marvel", "https://wsrv.nl/?url=https://upload.wikimedia.org/wikipedia/commons/thumb/b/b9/Marvel_Logo.svg/512px-Marvel_Logo.svg.png"),
        Universe("Star Wars", "https://wsrv.nl/?url=https://upload.wikimedia.org/wikipedia/commons/thumb/c/ce/Star_wars2.svg/512px-Star_wars2.svg.png"),
        Universe("Avatar", "https://wsrv.nl/?url=https://upload.wikimedia.org/wikipedia/commons/thumb/1/18/Avatar_logo.svg/512px-Avatar_logo.svg.png")
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
                            // TODO: Naviguer vers la liste des films de cet univers
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // C'est ici que la magie opère : on télécharge l'image depuis internet !
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