package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.data.TmdbClient
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.data.TmdbMovie
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Cache pour éviter que l'API ne soit rappelée à chaque changement d'onglet
object SessionCache {
    var recommendedMoviesCache: List<TmdbMovie>? = null
}

@Composable
fun HomeScreen(navController: NavController) {
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"

    var recommendedMovies by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    // 1. Tes 3 nouveautés pour le carrousel
    val latestReleases = listOf(
        Pair("The Mandalorian", "https://image.tmdb.org/t/p/w780/kKxkH0GdlN0K1Q22Xl0B2iZz8A.jpg"),
        Pair("Avengers: Endgame", "https://image.tmdb.org/t/p/w780/7RyHsO4yDXtBv1zUU3mTpHeQ0d5.jpg"),
        Pair("Avatar : La Voie de l'eau", "https://image.tmdb.org/t/p/w780/8rpDcsfLJypbO6vtec021P2NZYV.jpg")
    )

    // État du carrousel pour l'animation automatique
    val pagerState = rememberPagerState(pageCount = { latestReleases.size })

    // Animation automatique du carrousel toutes les 3 secondes
    LaunchedEffect(pagerState) {
        while (true) {
            delay(3000) // Attend 3 secondes
            val nextPage = (pagerState.currentPage + 1) % latestReleases.size
            pagerState.animateScrollToPage(nextPage) // Défilement fluide vers la page suivante
        }
    }

    LaunchedEffect(Unit) {
        if (SessionCache.recommendedMoviesCache == null) {
            coroutineScope.launch {
                try {
                    val response = TmdbClient.apiService.discoverMovies(myApiKey, "2")
                    SessionCache.recommendedMoviesCache = response.results.shuffled().take(10)
                    recommendedMovies = SessionCache.recommendedMoviesCache!!
                } catch (e: Exception) {
                    println("Erreur API: ${e.message}")
                }
            }
        } else {
            recommendedMovies = SessionCache.recommendedMoviesCache!!
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1D29)) // Couleur de fond style Disney+
    ) {
        // --- 1. CARROUSEL : DERNIÈRES SORTIES (Animé) ---
        item {
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 24.dp), // Permet de voir un peu l'image suivante
                pageSpacing = 16.dp, // Espace entre les images
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp)
            ) { page ->
                val movie = latestReleases[page]
                AsyncImage(
                    model = movie.second,
                    contentDescription = movie.first,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            navController.navigate("movie/${movie.first}/2023/Action")
                        }
                )
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

        // --- 3. CARROUSEL : RECOMMANDÉS POUR VOUS ---
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
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE50914))
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recommendedMovies) { movie ->
                        val imageUrl = "https://image.tmdb.org/t/p/w500${movie.poster_path}"

                        AsyncImage(
                            model = imageUrl,
                            contentDescription = movie.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(120.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val annee = movie.release_date?.take(4) ?: "Inconnue"
                                    navController.navigate("movie/${movie.title}/$annee/Disney")
                                }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun CategoryCard(title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(60.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF31343E))
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