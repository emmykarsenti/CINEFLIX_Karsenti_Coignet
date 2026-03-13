package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// CACHE DE SESSION (Pour ne pas spammer l'API)
object SessionCache {
    var latestReleasesCache: List<TmdbMovie>? = null
    var popularMoviesCache: List<TmdbMovie>? = null
    var recommendedMoviesCache: List<TmdbMovie>? = null
}

@Composable
fun HomeScreen(navController: NavController) {
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"

    // Les IDs de tes univers : Disney(2), Pixar(3), Marvel(420), StarWars(1), Avatar(574)
    val myUniverses = "2|3|420|1|574"

    // Les 3 listes qui vont contenir nos films
    var latestReleases by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }
    var popularMovies by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }
    var recommendedMovies by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    // État pour contrôler l'animation du carrousel du haut
    val listState = rememberLazyListState()

    // Date du jour formatée pour l'API (ex: "2026-03-13")
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // CHARGEMENT DES DONNÉES (Au lancement de l'écran)
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                // 1. LES PLUS RÉCENTS (Carrousel du haut)
                if (SessionCache.latestReleasesCache == null) {
                    val responseReleases = TmdbClient.apiService.discoverMovies(
                        apiKey = myApiKey,
                        companyId = myUniverses,
                        sortBy = "primary_release_date.desc", // Tri par date de sortie la plus récente
                        maxDate = todayDate // Bloque les films qui ne sont pas encore sortis
                    )
                    SessionCache.latestReleasesCache = responseReleases.results
                        .filter { it.backdrop_path != null } // On veut que des images larges (paysage)
                        .take(5) // On garde les 5 plus récents
                }
                latestReleases = SessionCache.latestReleasesCache!!

                // 2. LES PLUS POPULAIRES (Ligne du milieu)
                if (SessionCache.popularMoviesCache == null) {
                    val responsePopular = TmdbClient.apiService.discoverMovies(
                        apiKey = myApiKey,
                        companyId = myUniverses,
                        sortBy = "popularity.desc" // Tri par les films qui cartonnent le plus en ce moment
                    )
                    SessionCache.popularMoviesCache = responsePopular.results
                        .filter { it.poster_path != null } // On veut que des affiches verticales (portrait)
                        .take(10)
                }
                popularMovies = SessionCache.popularMoviesCache!!

                // 3. RECOMMANDÉS POUR VOUS (Ligne du bas)
                if (SessionCache.recommendedMoviesCache == null) {
                    val responseRecs = TmdbClient.apiService.discoverMovies(myApiKey, "2|3|420|1|574")
                    // On mélange les résultats pour avoir de la diversité
                    SessionCache.recommendedMoviesCache = responseRecs.results.shuffled().take(10)
                }
                recommendedMovies = SessionCache.recommendedMoviesCache!!

            } catch (e: Exception) {
                println("Erreur API: ${e.message}")
            }
        }
    }

    // MOTEUR D'ANIMATION DU CARROUSEL
    LaunchedEffect(latestReleases.size) {
        if (latestReleases.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(6000) // Attente de 6 secondes
                val currentIndex = listState.firstVisibleItemIndex
                val nextIndex = (currentIndex + 1) % latestReleases.size
                listState.animateScrollToItem(nextIndex) // Défilement doux vers l'image suivante
            }
        }
    }

    // INTERFACE UTILISATEUR (UI)
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1A1D29)) // Couleur de fond style Disney+
    ) {

        // SECTION 1 : CARROUSEL DES NOUVEAUTÉS (Images larges)
        item {
            if (latestReleases.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(248.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE50914))
                }
            } else {
                LazyRow(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(latestReleases) { movie ->
                        // On utilise backdrop_path pour avoir le format paysage
                        val backdropUrl = "https://image.tmdb.org/t/p/w780${movie.backdrop_path}"

                        AsyncImage(
                            model = backdropUrl,
                            contentDescription = movie.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillParentMaxWidth(0.9f) // Prend 90% de l'écran pour laisser deviner la suite
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF31343E)) // Couleur grise pendant le chargement
                                .clickable {
                                    val annee = movie.release_date?.take(4) ?: "Inconnue"
                                    navController.navigate("movie/${movie.title}/$annee/Nouveauté")
                                }
                        )
                    }
                }
            }
        }

        // SECTION 2 : BOUTONS DES UNIVERS
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

        // SECTION 3 : LES PLUS POPULAIRES (Affiches verticales)
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Les plus populaires en ce moment",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (popularMovies.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE50914))
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(popularMovies) { movie ->
                        // On utilise poster_path pour avoir le format portrait classique
                        val posterUrl = "https://image.tmdb.org/t/p/w500${movie.poster_path}"

                        AsyncImage(
                            model = posterUrl,
                            contentDescription = movie.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(120.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF31343E))
                                .clickable {
                                    val annee = movie.release_date?.take(4) ?: "Inconnue"
                                    navController.navigate("movie/${movie.title}/$annee/Populaire")
                                }
                        )
                    }
                }
            }
        }

        // SECTION 4 : RECOMMANDÉS POUR VOUS (Affiches verticales)
        item {
            Spacer(modifier = Modifier.height(24.dp))
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
                        val posterUrl = "https://image.tmdb.org/t/p/w500${movie.poster_path}"

                        AsyncImage(
                            model = posterUrl,
                            contentDescription = movie.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(120.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF31343E))
                                .clickable {
                                    val annee = movie.release_date?.take(4) ?: "Inconnue"
                                    navController.navigate("movie/${movie.title}/$annee/Recommandé")
                                }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp)) // Espace final pour ne pas être bloqué par la barre de navigation
        }
    }
}

// COMPOSANT : BOUTON DE CATÉGORIE
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