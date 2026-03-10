package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    // Ta clé API TMDB officielle
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"

    // Variables pour stocker les films récupérés depuis l'API
    var recommendedMovies by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    // Bannières "Dernières sorties" (Format paysage idéal pour le haut de l'écran)
    val latestReleases = listOf(
        Pair("The Mandalorian", "https://image.tmdb.org/t/p/w780/kKxkH0GdlN0K1Q22Xl0B2iZz8A.jpg"),
        Pair("Avengers: Endgame", "https://image.tmdb.org/t/p/w780/7RyHsO4yDXtBv1zUU3mTpHeQ0d5.jpg"),
        Pair("Avatar : La Voie de l'eau", "https://image.tmdb.org/t/p/w780/8rpDcsfLJypbO6vtec021P2NZYV.jpg")
    )

    // Appel à l'API TMDB au lancement de l'écran
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                // ID 2 = Walt Disney Pictures
                val response = TmdbClient.apiService.discoverMovies(myApiKey, "2")
                recommendedMovies = response.results
            } catch (e: Exception) {
                println("Erreur API: ${e.message}")
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1D29)) // Couleur de fond style Disney+
    ) {
        // --- 1. CARROUSEL : DERNIÈRES SORTIES ---
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(latestReleases) { movie ->
                    AsyncImage(
                        model = movie.second,
                        contentDescription = movie.first,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillParentMaxWidth(0.9f) // Prend 90% de l'écran pour laisser entrevoir la suivante
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                // Navigation basique si on clique sur la bannière
                                navController.navigate("movie/${movie.first}/2023/Action")
                            }
                    )
                }
            }
        }

        // --- 2. GRILLE : CATÉGORIES / UNIVERS ---
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryCard("Disney", Modifier.weight(1f)) { navController.navigate("universe/Disney") }
                    CategoryCard("Pixar", Modifier.weight(1f)) { navController.navigate("universe/Pixar") }
                    CategoryCard("Marvel", Modifier.weight(1f)) { navController.navigate("universe/Marvel") }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryCard("Star Wars", Modifier.weight(1f)) { navController.navigate("universe/Star Wars") }
                    CategoryCard("Avatar", Modifier.weight(1f)) { navController.navigate("universe/Avatar") }
                    CategoryCard("Nat Geo", Modifier.weight(1f)) { navController.navigate("universe/Nat Geo") }
                }
            }
        }

        // --- 3. CARROUSEL : RECOMMANDÉS POUR VOUS (Généré via API) ---
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Recommandés pour vous",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (recommendedMovies.isEmpty()) {
                // Animation de chargement en attendant la réponse de l'API
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE50914))
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recommendedMovies) { movie ->
                        // L'API nous donne juste le nom du fichier, on ajoute l'URL de base TMDB
                        val imageUrl = "https://image.tmdb.org/t/p/w500${movie.poster_path}"

                        AsyncImage(
                            model = imageUrl,
                            contentDescription = movie.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(120.dp)
                                .height(180.dp) // Ratio portrait (Affiche)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    // On récupère l'année de sortie depuis l'API (les 4 premiers caractères de la date)
                                    val annee = movie.release_date?.take(4) ?: "Inconnue"
                                    navController.navigate("movie/${movie.title}/$annee/Disney")
                                }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Sous-composant pour les boutons de catégories
@Composable
fun CategoryCard(title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(60.dp)
            .clickable { onClick() }, // Rend toute la case cliquable
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF31343E)) // Gris foncé
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}