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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

object SessionCache {
    var latestReleasesCache: List<TmdbMovie>? = null
    var popularMoviesCache: List<TmdbMovie>? = null
    var recommendedMoviesCache: List<TmdbMovie>? = null
    var comedyMoviesCache: List<TmdbMovie>? = null
    var actionMoviesCache: List<TmdbMovie>? = null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"
    val myUniverses = "2|3|420|1|574"

    var latestReleases by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }
    var popularMovies by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }
    var recommendedMovies by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }
    var comedyMovies by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }
    var actionMovies by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }

    // États pour le nouveau menu Filtre
    var showFilterMenu by remember { mutableStateOf(false) }
    val selectedCategories = remember { mutableStateListOf<String>() }

    val allCategories = listOf("Disney", "Pixar", "Marvel", "Star Wars", "Action", "Comédie", "Animation", "Science-Fiction")

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

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

                if (SessionCache.comedyMoviesCache == null) {
                    val responseComedy = TmdbClient.apiService.discoverMovies(apiKey = myApiKey, companyId = myUniverses, withGenres = "35", sortBy = "popularity.desc")
                    SessionCache.comedyMoviesCache = responseComedy.results.filter { it.poster_path != null }.take(7)
                }
                comedyMovies = SessionCache.comedyMoviesCache!!

                if (SessionCache.actionMoviesCache == null) {
                    val responseAction = TmdbClient.apiService.discoverMovies(apiKey = myApiKey, companyId = myUniverses, withGenres = "28", sortBy = "popularity.desc")
                    SessionCache.actionMoviesCache = responseAction.results.filter { it.poster_path != null }.take(7)
                }
                actionMovies = SessionCache.actionMoviesCache!!

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            delay(250)
            try {
                val response = TmdbClient.apiService.searchMovie(myApiKey, searchQuery)

                // Filtre anti-intrus (Horreur: 27, Thriller: 53, Crime: 80)
                val forbiddenGenres = listOf(27, 53, 80)

                searchResults = response.results.filter { movie ->
                    movie.poster_path != null &&
                            movie.genre_ids?.none { id -> id in forbiddenGenres } == true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            searchResults = emptyList()
        }
    }

    // Application des filtres sélectionnés dans le menu sur les résultats de recherche
    val filteredSearchResults = searchResults.filter { movie ->
        if (selectedCategories.isEmpty()) return@filter true

        val movieGenres = movie.genre_ids ?: emptyList()
        var matches = false

        if ("Action" in selectedCategories && 28 in movieGenres) matches = true
        if ("Comédie" in selectedCategories && 35 in movieGenres) matches = true
        if ("Animation" in selectedCategories && 16 in movieGenres) matches = true
        if ("Science-Fiction" in selectedCategories && 878 in movieGenres) matches = true

        // Pour les studios, on laisse afficher car la recherche TMDB textuelle ne précise pas le studio
        if ("Disney" in selectedCategories || "Pixar" in selectedCategories || "Marvel" in selectedCategories || "Star Wars" in selectedCategories) matches = true

        matches
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1A1D29))) {

        if (!isSearchActive) {
            Image(
                painter = painterResource(id = R.drawable.logo_cineflix_homescreen),
                contentDescription = "Logo Cineflix",
                modifier = Modifier.height(130.dp).fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                contentScale = ContentScale.Fit
            )
        }

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = if (isSearchActive) 0.dp else 16.dp, vertical = 8.dp)) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = { isSearchActive = false },
                        expanded = isSearchActive,
                        onExpandedChange = { isSearchActive = it },
                        placeholder = { Text("Rechercher un film...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Rechercher") },
                        trailingIcon = {
                            if (isSearchActive) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Fermer",
                                    modifier = Modifier.clickable {
                                        if (searchQuery.isNotEmpty()) searchQuery = "" else isSearchActive = false
                                    }
                                )
                            }
                        }
                    )
                },
                expanded = isSearchActive,
                onExpandedChange = { isSearchActive = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                // BOUTON POUR OUVRIR LE NOUVEAU MENU FILTRE
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showFilterMenu = true }) {
                        Text(if (selectedCategories.isEmpty()) "FILTRER" else "FILTRER (${selectedCategories.size})", color = Color(0xFFFCA311), fontWeight = FontWeight.Bold)
                    }
                }

                // Résultats de recherche (Maintenant filtrés !)
                LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(filteredSearchResults) { movie ->
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
            }
        }

        if (!isSearchActive) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    LazyRow(state = listState, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(count = Int.MAX_VALUE) { index ->
                            if (latestReleases.isNotEmpty()) {
                                val movie = latestReleases[index % latestReleases.size]
                                val backdropUrl = "https://image.tmdb.org/t/p/w780${movie.backdrop_path}"

                                AsyncImage(
                                    model = backdropUrl,
                                    contentDescription = movie.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillParentMaxWidth(0.9f).height(200.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF31343E))
                                        .clickable {
                                            val annee = movie.release_date?.take(4) ?: "Inconnue"
                                            val safeTitre = Uri.encode(movie.title.ifBlank { "Inconnu" })
                                            navController.navigate("movie/$safeTitre/$annee/Nouveauté/Inconnue/Inconnu/Pop Culture")
                                        }
                                )
                            }
                        }
                    }
                }

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

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Comédies", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Voir plus", color = Color(0xFFE50914), fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable {
                            navController.navigate("genre/Comédie/35")
                        })
                    }
                    MovieCarouselRow(movies = comedyMovies, navController = navController, genreTag = "Comédie")
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Films d'Action", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Voir plus", color = Color(0xFFE50914), fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable {
                            navController.navigate("genre/Action/28")
                        })
                    }
                    MovieCarouselRow(movies = actionMovies, navController = navController, genreTag = "Action")
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }

    // --- LE NOUVEAU MENU FILTRE PLEIN ÉCRAN ---
    if (showFilterMenu) {
        Dialog(
            onDismissRequest = { showFilterMenu = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E1E))
                    .padding(24.dp)
            ) {
                // Header (Titre + Croix)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("FILTER", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = Color.White,
                        modifier = Modifier.clickable { showFilterMenu = false }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("CATEGORY", color = Color(0xFFFCA311), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // Liste des Checkboxes
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(allCategories) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clickable {
                                    if (selectedCategories.contains(category)) selectedCategories.remove(category)
                                    else selectedCategories.add(category)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedCategories.contains(category),
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFFFCA311),
                                    uncheckedColor = Color.LightGray,
                                    checkmarkColor = Color.Black
                                )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(category, color = Color.White, fontSize = 16.sp)
                        }
                    }
                }

                // Boutons en bas
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { showFilterMenu = false },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCA311)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("SHOW RESULTS", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { selectedCategories.clear() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
                    ) {
                        Text("CLEAR FILTERS", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

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
                            navController.navigate("movie/$safeTitre/$annee/${Uri.encode(genreTag)}/Inconnue/Inconnu/Pop Culture")
                        }
                )
            }
        }
    }
}

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