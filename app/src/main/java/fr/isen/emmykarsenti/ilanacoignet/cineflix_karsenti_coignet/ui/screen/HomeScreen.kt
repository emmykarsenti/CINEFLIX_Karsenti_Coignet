package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.R
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.data.TmdbClient
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.data.TmdbMovie
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* * NOTE IMPORTANTE CONCERNANT LES FRANCHISES :
 * Nous avons voulu restreindre l'intégralité de l'application aux franchises
 * Disney, Pixar, Marvel, Star Wars et Avatar. Cependant, l'API TMDB présente des limites.
 * Si le endpoint "discover" permet d'utiliser un "companyId", le endpoint de recherche
 * textuelle ("search/movie") ne permet PAS de filtrer par studio. Par conséquent,
 * lors d'une recherche, des films n'appartenant pas à nos franchises peuvent apparaître.
 * C'est une limitation directe de l'API TMDB que nous ne pouvons pas contourner.
 */

// Objet de cache pour éviter de recharger l'API à chaque recomposition de l'écran d'accueil
object SessionCache {
    var latestReleasesCache: List<TmdbMovie>? = null
    var popularMoviesCache: List<TmdbMovie>? = null
    var recommendedMoviesCache: List<TmdbMovie>? = null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    // Clé API TMDB et IDs des studios ciblés
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"
    val myUniverses = "2|3|420|1|574" // Disney, Pixar, Marvel, StarWars, Avatar

    // États pour stocker les listes de films de l'accueil
    var latestReleases by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }
    var popularMovies by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }
    var recommendedMovies by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }

    // États pour gérer la barre de recherche
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // 1. Chargement initial de l'en-tête (Nouveautés, Populaires, Recommandés)
    // On utilise le cache pour limiter les requêtes réseau
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                if (SessionCache.latestReleasesCache == null) {
                    val responseReleases = TmdbClient.apiService.discoverMovies(apiKey = myApiKey, companyId = myUniverses, sortBy = "primary_release_date.desc", maxDate = todayDate)
                    SessionCache.latestReleasesCache = responseReleases.results.filter { it.backdrop_path != null }.take(5)
                }
                latestReleases = SessionCache.latestReleasesCache!!

                if (SessionCache.popularMoviesCache == null) {
                    val responsePopular = TmdbClient.apiService.discoverMovies(apiKey = myApiKey, companyId = myUniverses, sortBy = "popularity.desc")
                    SessionCache.popularMoviesCache = responsePopular.results.filter { it.poster_path != null }.take(10)
                }
                popularMovies = SessionCache.popularMoviesCache!!

                if (SessionCache.recommendedMoviesCache == null) {
                    val responseRecs = TmdbClient.apiService.discoverMovies(apiKey = myApiKey, companyId = myUniverses)
                    SessionCache.recommendedMoviesCache = responseRecs.results.shuffled().take(10)
                }
                recommendedMovies = SessionCache.recommendedMoviesCache!!

            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // 2. Gestion de la Recherche Textuelle (Appel API dynamique)
    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            delay(250) // Délai anti-spam (debounce) pour ne pas saturer l'API à chaque lettre tapée
            try {
                // RAPPEL LIMITATION TMDB : Impossible de forcer 'myUniverses' ici.
                val response = TmdbClient.apiService.searchMovie(myApiKey, searchQuery)
                val forbiddenGenres = listOf(27, 53, 80) // Exclusion manuelle des genres indésirables (Horreur, Thriller, Crime)
                searchResults = response.results.filter { movie ->
                    movie.poster_path != null && movie.genre_ids?.none { id -> id in forbiddenGenres } == true
                }
            } catch (e: Exception) { e.printStackTrace() }
        } else {
            searchResults = emptyList() // On vide les résultats si la recherche est trop courte
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1A1D29))) {

        // Logo de l'application en haut de l'écran
        Image(
            painter = painterResource(id = R.drawable.logo_cineflix_homescreen),
            contentDescription = "Logo Cineflix",
            modifier = Modifier.height(110.dp).fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
            contentScale = ContentScale.Fit
        )

        // BARRE DE RECHERCHE FIXE
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Rechercher un film...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    // Bouton pour effacer la recherche
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = "Effacer", tint = Color.White) }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFCA311),
                    unfocusedBorderColor = Color(0xFF31343E),
                    focusedContainerColor = Color(0xFF31343E),
                    unfocusedContainerColor = Color(0xFF31343E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // AFFICHAGE CONDITIONNEL : Si l'utilisateur cherche, on affiche la liste des résultats.
        // Sinon, on affiche la page d'accueil avec les carrousels.
        if (searchQuery.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(searchResults) { movie ->
                    Row(modifier = Modifier.fillMaxWidth().clickable {
                        val safeTitre = Uri.encode(movie.title.ifBlank { "Inconnu" })
                        navController.navigate("movie/$safeTitre/Inconnue/Recherche/Inconnue/Inconnu/Pop Culture")
                    }, verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w200${movie.poster_path}",
                            contentDescription = movie.title,
                            modifier = Modifier.width(60.dp).height(90.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(movie.title, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(movie.release_date?.take(4) ?: "", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // PAGE D'ACCUEIL CLASSIQUE
            LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {

                // 1. Carrousel des nouveautés (Bannières larges)
                item {
                    LazyRow(state = listState, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(count = Int.MAX_VALUE) { index -> // Boucle infinie simulée
                            if (latestReleases.isNotEmpty()) {
                                val movie = latestReleases[index % latestReleases.size]
                                AsyncImage(
                                    model = "https://image.tmdb.org/t/p/w780${movie.backdrop_path}",
                                    contentDescription = movie.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillParentMaxWidth(0.9f).height(200.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF31343E))
                                        .clickable {
                                            val annee = movie.release_date?.take(4) ?: "Inconnue"
                                            navController.navigate("movie/${Uri.encode(movie.title)}/$annee/Nouveauté/Inconnue/Inconnu/Pop Culture")
                                        }
                                )
                            }
                        }
                    }
                }

                // 2. Grille de navigation des univers (Studios)
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CategoryCard("Disney", Modifier.weight(1f)) { navController.navigate("universe/Disney") }
                            CategoryCard("Pixar", Modifier.weight(1f)) { navController.navigate("universe/Pixar") }
                            CategoryCard("Marvel", Modifier.weight(1f)) { navController.navigate("universe/Marvel") }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CategoryCard("Star Wars", Modifier.weight(1f)) { navController.navigate("universe/Star Wars") }
                            CategoryCard("Avatar", Modifier.weight(1f)) { navController.navigate("universe/Avatar") }
                            CategoryCard("Voir tous", Modifier.weight(1f)) { navController.navigate("universe/Toutes Catégories") }
                        }
                    }
                }

                // 3. Carrousels standards (Populaires & Recommandés)
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Les plus populaires", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    MovieCarouselRow(movies = popularMovies, navController = navController, genreTag = "Populaire")
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Recommandés pour vous", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    MovieCarouselRow(movies = recommendedMovies, navController = navController, genreTag = "Recommandé")
                }

                // 4. Génération automatique des carrousels pour chaque genre ciblé
                val listOfAllGenres = listOf(
                    "Action" to "28", "Animation & Dessin Animé" to "16", "Aventure" to "12", "Comédie" to "35",
                    "Comédie Dramatique" to "35|18", "Comédie Musicale" to "10402", "Documentaire" to "99",
                    "Drame" to "18", "Fantastique" to "14", "Guerre" to "10752", "Historique & Biographique" to "36",
                    "Policier" to "80", "Romance" to "10749", "Science-Fiction" to "878", "Téléfilm" to "10770",
                    "Thriller" to "53", "Western" to "37"
                )

                items(listOfAllGenres) { (genreName, genreId) ->
                    GenreDynamicRow(genreName, genreId, navController, myApiKey, myUniverses)
                }

                // Espace vide en bas pour éviter que le contenu ne soit caché par la barre de navigation
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

// COMPOSANTS UI RÉUTILISABLES

/**
 * Composant intelligent gérant le chargement asynchrone d'une rangée de films par genre.
 * Gère également l'affichage du titre et du bouton "Voir plus".
 */
@Composable
fun GenreDynamicRow(genreName: String, genreId: String, navController: NavController, apiKey: String, universes: String) {
    var movies by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }

    LaunchedEffect(genreId) {
        try {
            // Ici le filtre par studio fonctionne car nous utilisons l'endpoint 'discover'
            val response = TmdbClient.apiService.discoverMovies(apiKey = apiKey, companyId = universes, withGenres = genreId, sortBy = "popularity.desc")
            movies = response.results.filter { it.poster_path != null }.take(10)
        } catch (e: Exception) { e.printStackTrace() }
    }

    // On n'affiche la section que si des films ont été trouvés
    if (movies.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(genreName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Voir plus", color = Color(0xFFF299B5), fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable {
                navController.navigate("genre/${Uri.encode(genreName)}/$genreId")
            })
        }
        MovieCarouselRow(movies = movies, navController = navController, genreTag = genreName)
    }
}

/**
 * Composant affichant une liste horizontale (carrousel) d'affiches de films.
 */
@Composable
fun MovieCarouselRow(movies: List<TmdbMovie>, navController: NavController, genreTag: String) {
    if (movies.isNotEmpty()) {
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(movies) { movie ->
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.width(120.dp).height(180.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF31343E))
                        .clickable {
                            val annee = movie.release_date?.take(4) ?: "Inconnue"
                            val safeTitre = Uri.encode(movie.title.ifBlank { "Inconnu" })
                            // Navigation vers l'écran de détails du film
                            navController.navigate("movie/$safeTitre/$annee/${Uri.encode(genreTag)}/Inconnue/Inconnu/Pop Culture")
                        }
                )
            }
        }
    }
}

/**
 * Bouton de catégorie stylisé (ex: Disney, Pixar, etc.)
 */
@Composable
fun CategoryCard(title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(60.dp).clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF31343E))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}