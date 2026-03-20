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

//app centrée sur disney, pixar, marvel, star wars et avatar
//l'endpoint "discover" de tmdb permet de filtrer par studio, mais pas l'endpoint "search": donc quand on fait une recherche dans la barre de recherche, des films hors franchise peuvent apparaître (limite de l'api qu'on ne peut pas contourner...)


// objets de cache pour ne pas à avoir à recharger l'api à chaque affichage de l'écran d'accueil
object SessionCache {
    var latestReleasesCache: List<TmdbMovie>? = null
    var popularMoviesCache: List<TmdbMovie>? = null
    var recommendedMoviesCache: List<TmdbMovie>? = null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    // key api tmdb et id des studios
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"
    val myUniverses = "2|3|420|1|574" // Disney, Pixar, Marvel, StarWars, Avatar

    // états pour stocker les listes de films de l'accueil
    var latestReleases by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }
    var popularMovies by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }
    var recommendedMovies by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }

    // états pour gérer la barre de recherche
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    //chargement de base de l'en-tête (type nouveautés, populaires, recommandés)
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                if (SessionCache.latestReleasesCache == null) { //nouveautés triées par date de sortie
                    val responseReleases = TmdbClient.apiService.discoverMovies(apiKey = myApiKey, companyId = myUniverses, sortBy = "primary_release_date.desc", maxDate = todayDate)
                    SessionCache.latestReleasesCache = responseReleases.results.filter { it.backdrop_path != null }.take(5)
                }
                latestReleases = SessionCache.latestReleasesCache!!

                if (SessionCache.popularMoviesCache == null) { // popularité des films/series
                    val responsePopular = TmdbClient.apiService.discoverMovies(apiKey = myApiKey, companyId = myUniverses, sortBy = "popularity.desc")
                    SessionCache.popularMoviesCache = responsePopular.results.filter { it.poster_path != null }.take(10)
                }
                popularMovies = SessionCache.popularMoviesCache!!

                if (SessionCache.recommendedMoviesCache == null) { //recommandations (qui varient)
                    val responseRecs = TmdbClient.apiService.discoverMovies(apiKey = myApiKey, companyId = myUniverses)
                    SessionCache.recommendedMoviesCache = responseRecs.results.shuffled().take(10)
                }
                recommendedMovies = SessionCache.recommendedMoviesCache!!

            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    //pour gérer la recherche par texte (appel api dynamique): on attend que le user rentre au moins 3 caractères
    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            delay(250) // délai pour le debounce pour ne pas spammmer l'api à chaque lettre tapée
            try {
                val response = TmdbClient.apiService.searchMovie(myApiKey, searchQuery)
                val forbiddenGenres = listOf(27, 53, 80) // exclusion des genres "indésirables" (horreur, thriller, crime...)
                searchResults = response.results.filter { movie ->
                    movie.poster_path != null && movie.genre_ids?.none { id -> id in forbiddenGenres } == true
                }
            } catch (e: Exception) { e.printStackTrace() }
        } else {
            searchResults = emptyList() // on vide les résultats si la recherche est trop courte
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1A1D29))) {

        // logo cineflix
        Image(
            painter = painterResource(id = R.drawable.logo_cineflix_homescreen),
            contentDescription = "Logo Cineflix",
            modifier = Modifier.height(110.dp).fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
            contentScale = ContentScale.Fit
        )

        //barre de recherche fixe
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
                    // bouton pour effacer la recherche
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

        // si le user cherche quelque chose, on affiche la liste des résultats. Sinon, on affiche la page d'accueil normale.
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
            // page d'accueil avec carrousel
            LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {

                // carrousel des nouveautés sous forme de banière
                item {
                    LazyRow(state = listState, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(count = Int.MAX_VALUE) { index -> // on simule une boucle infinie
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

                // grille pour la navigation dans les univers
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

                // carrousels des films populaires
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Les plus populaires", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    MovieCarouselRow(movies = popularMovies, navController = navController, genreTag = "Populaire")
                }
                // carrousel des recommandations
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Recommandés pour vous", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    MovieCarouselRow(movies = recommendedMovies, navController = navController, genreTag = "Recommandé")
                }

                // carrousels par genre générés automatiquement
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

                // cet esapce nous permet d'éviter de cacher le contenu par la barre de navbar
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

//carrousel par genre (charge les films dynamiquement et affiche le titre + "voir plus")
@Composable
fun GenreDynamicRow(genreName: String, genreId: String, navController: NavController, apiKey: String, universes: String) {
    var movies by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }

    LaunchedEffect(genreId) {
        try {
            val response = TmdbClient.apiService.discoverMovies(apiKey = apiKey, companyId = universes, withGenres = genreId, sortBy = "popularity.desc")
            movies = response.results.filter { it.poster_path != null }.take(10)
        } catch (e: Exception) { e.printStackTrace() }
    }

    // affichage de la section que si des films ont été trouvés par ce genre
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

//rangée horizontale d'affiches de films cliquables
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

//bouton de catégorie disney, pixar, etc...
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