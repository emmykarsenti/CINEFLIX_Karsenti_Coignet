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

//affiche tous les films d'un genre donné (ex: action, comédie) sous forme de grille
@OptIn(ExperimentalMaterial3Api::class) // Nécessaire pour utiliser TopAppBar qui est encore expérimentale dans Material 3
@Composable
fun GenreScreen(navController: NavController, genreName: String, genreId: String) {
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512" // clé api tmdb pour autoriser les requêtes
    var movies by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) } // movies stocke la liste des films récupérés depuis l'api puis on met à jour l'ui quand la liste change
    var isLoading by remember { mutableStateOf(true) } //affiche un indicateur de chargement pendant la requête api
    val coroutineScope = rememberCoroutineScope()

    //chargement des films au lancement ou si le genre change
    LaunchedEffect(genreId) {
        coroutineScope.launch {
            try {
                val myUniverses = "2|3|420|1|574" // disney, pixar, marvel, star wars, avatar

                //on utilise "discover"pour pouvoir filtrer par studio et par genre
                val response = TmdbClient.apiService.discoverMovies(
                    apiKey = myApiKey,
                    companyId = myUniverses,
                    withGenres = genreId,
                    sortBy = "popularity.desc" // trie les résultats par popularité décroissante
                )

                //on garde que les films avec affiches
                movies = response.results.filter { it.poster_path != null }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                //on arrête le chargement que la requête ait réussi ou pas
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
            // affichage de l'indicateur de chargement rouge centré à l'écran
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE50914))
            }
        } else {
            // affichage des films sous forme de grille quand le chargement terminé
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // parcourt la liste des films récupérés
                items(movies) { movie ->
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop, // recadre l'affiche sans déformation
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f) //ratio standard de poster de film
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF31343E))
                            .clickable {
                                // clic sur une affiche -> écran de détail du film -> récupère l'année de sortie
                                val annee = movie.release_date?.take(4) ?: "Inconnue"
                                val safeTitre = Uri.encode(movie.title.ifBlank { "Inconnu" })
                                navController.navigate("movie/$safeTitre/$annee/${Uri.encode(genreName)}/Inconnue/Inconnu/Pop Culture")  // navigation vers la route MovieDetailScreen avec tous ses paramètres requis
                            }
                    )
                }
            }
        }
    }
}