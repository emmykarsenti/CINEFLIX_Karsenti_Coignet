package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreScreen(navController: NavController, genreName: String, genreId: String) {
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"
    var movies by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(genreId) {
        coroutineScope.launch {
            try {
                // On rajoute la liste de tes studios ici !
                val myUniverses = "2|3|420|1|574"

                val response = TmdbClient.apiService.discoverMovies(
                    apiKey = myApiKey,
                    companyId = myUniverses, // <-- Et on force l'API à n'utiliser que ces studios
                    withGenres = genreId,
                    sortBy = "popularity.desc"
                )
                movies = response.results.filter { it.poster_path != null }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Films : $genreName", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1D29),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1A1D29)
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE50914))
            }
        } else {
            // Affichage sous forme de grille de 3 colonnes (façon Netflix/Galerie)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(movies) { movie ->
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f) // Format affiche de film standard
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF31343E))
                            .clickable {
                                val annee = movie.release_date?.take(4) ?: "Inconnue"
                                val safeTitre = Uri.encode(movie.title.ifBlank { "Inconnu" })
                                navController.navigate("movie/$safeTitre/$annee/${Uri.encode(genreName)}/Inconnue/Inconnu/Pop Culture")
                            }
                    )
                }
            }
        }
    }
}