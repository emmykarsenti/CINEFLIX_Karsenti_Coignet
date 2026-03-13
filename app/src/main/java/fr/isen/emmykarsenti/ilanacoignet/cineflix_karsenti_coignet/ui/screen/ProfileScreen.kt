package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.data.PosterCache
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.data.TmdbClient
import kotlinx.coroutines.launch

data class MovieWithPoster(
    val title: String,
    val posterUrl: String?
)

@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val coroutineScope = rememberCoroutineScope()
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"

    var watchedMovies by remember { mutableStateOf<List<MovieWithPoster>>(emptyList()) }
    var wantToWatchMovies by remember { mutableStateOf<List<MovieWithPoster>>(emptyList()) }
    var ownedMovies by remember { mutableStateOf<List<MovieWithPoster>>(emptyList()) }
    var wantToSellMovies by remember { mutableStateOf<List<MovieWithPoster>>(emptyList()) }

    suspend fun fetchPoster(title: String): String? {
        PosterCache.posters[title]?.let { return it }
        return try {
            val result = TmdbClient.apiService.searchMovie(myApiKey, title)
            val path = result.results.firstOrNull()?.poster_path
            if (path != null) {
                val url = "https://image.tmdb.org/t/p/w500$path"
                PosterCache.posters[title] = url
                url
            } else null
        } catch (e: Exception) { null }
    }

    fun removeMovieFromFirebase(movieTitle: String) {
        val uid = currentUser?.uid ?: return
        FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies/$uid/$movieTitle")
            .removeValue()
    }

    LaunchedEffect(currentUser) {
        val uid = currentUser?.uid ?: return@LaunchedEffect
        FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies/$uid")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempWatched = mutableListOf<String>()
                    val tempWantToWatch = mutableListOf<String>()
                    val tempOwned = mutableListOf<String>()
                    val tempWantToSell = mutableListOf<String>()

                    for (movieSnap in snapshot.children) {
                        val title = movieSnap.key ?: continue
                        val wStatus = movieSnap.child("watch_status").getValue(String::class.java)
                        val oStatus = movieSnap.child("own_status").getValue(String::class.java)
                        if (wStatus == "WATCHED") tempWatched.add(title)
                        if (wStatus == "WANT_TO_WATCH") tempWantToWatch.add(title)
                        if (oStatus == "OWN_DVD") tempOwned.add(title)
                        if (oStatus == "WANT_TO_SELL") tempWantToSell.add(title)
                    }

                    coroutineScope.launch {
                        watchedMovies = tempWatched.map { MovieWithPoster(it, fetchPoster(it)) }
                        wantToWatchMovies = tempWantToWatch.map { MovieWithPoster(it, fetchPoster(it)) }
                        ownedMovies = tempOwned.map { MovieWithPoster(it, fetchPoster(it)) }
                        wantToSellMovies = tempWantToSell.map { MovieWithPoster(it, fetchPoster(it)) }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1D29))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mon Profil", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF299B5))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Connecté en tant que :", fontSize = 14.sp, color = Color.Gray)
        Text(
            text = currentUser?.email ?: "Utilisateur inconnu",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (watchedMovies.isEmpty() && wantToWatchMovies.isEmpty() && ownedMovies.isEmpty() && wantToSellMovies.isEmpty()) {
                item {
                    Text("Vous n'avez encore ajouté aucun film.", color = Color.Gray)
                }
            }

            if (ownedMovies.isNotEmpty()) {
                item { ProfileSectionTitle("Mes films possédés") }
                item { MoviePosterGrid(ownedMovies, navController) { removeMovieFromFirebase(it) } }
            }

            if (watchedMovies.isNotEmpty()) {
                item { ProfileSectionTitle("Films vus") }
                item { MoviePosterGrid(watchedMovies, navController) { removeMovieFromFirebase(it) } }
            }

            if (wantToWatchMovies.isNotEmpty()) {
                item { ProfileSectionTitle("À voir") }
                item { MoviePosterGrid(wantToWatchMovies, navController) { removeMovieFromFirebase(it) } }
            }

            if (wantToSellMovies.isNotEmpty()) {
                item { ProfileSectionTitle("À vendre") }
                item { MoviePosterGrid(wantToSellMovies, navController) { removeMovieFromFirebase(it) } }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                auth.signOut()
                navController.navigate("auth") { popUpTo(0) }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF299B5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Se déconnecter", fontSize = 14.sp, color = Color.White)
        }
    }
}

@Composable
fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun MoviePosterGrid(
    movies: List<MovieWithPoster>,
    navController: NavController,
    onDelete: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        movies.chunked(3).forEach { rowMovies ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowMovies.forEach { movie ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box {
                            AsyncImage(
                                model = movie.posterUrl ?: "",
                                contentDescription = movie.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(2f / 3f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF31343E))
                                    .clickable {
                                        movie.posterUrl?.let { PosterCache.posters[movie.title] = it }
                                        navController.navigate("movie/${movie.title}/-/-")
                                    }
                            )
                            IconButton(
                                onClick = { onDelete(movie.title) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Supprimer",
                                    tint = Color(0xFFFFFFFF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = movie.title,
                            color = Color.White,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // Remplir les cases vides si la dernière ligne n'est pas complète
                repeat(3 - rowMovies.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}