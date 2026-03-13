/*package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var watchedMovies by remember { mutableStateOf<List<String>>(emptyList()) }
    var wantToWatchMovies by remember { mutableStateOf<List<String>>(emptyList()) }
    var ownedMovies by remember { mutableStateOf<List<String>>(emptyList()) }
    var wantToSellMovies by remember { mutableStateOf<List<String>>(emptyList()) }

    fun removeMovieFromFirebase(movieTitle: String) {
        val uid = currentUser?.uid ?: return
        FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies/$uid/$movieTitle")
            .removeValue() // Supprime l'objet entier du film (statut de visionnage + possession)
    }

    LaunchedEffect(currentUser) {
        val uid = currentUser?.uid
        if (uid != null) {
            val dbRef = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("userMovies/$uid")

            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempWatched = mutableListOf<String>()
                    val tempWantToWatch = mutableListOf<String>()
                    val tempOwned = mutableListOf<String>()
                    val tempWantToSell = mutableListOf<String>()

                    for (movieSnap in snapshot.children) {
                        val title = movieSnap.key ?: continue

                        // NOUVEAU : On lit les deux propriétés du film
                        val wStatus = movieSnap.child("watch_status").getValue(String::class.java)
                        val oStatus = movieSnap.child("own_status").getValue(String::class.java)

                        if (wStatus == "WATCHED") tempWatched.add(title)
                        if (wStatus == "WANT_TO_WATCH") tempWantToWatch.add(title)

                        if (oStatus == "OWN_DVD") tempOwned.add(title)
                        if (oStatus == "WANT_TO_SELL") tempWantToSell.add(title)
                    }

                    watchedMovies = tempWatched
                    wantToWatchMovies = tempWantToWatch
                    ownedMovies = tempOwned
                    wantToSellMovies = tempWantToSell
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Mon Profil", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Connecté en tant que :", fontSize = 16.sp)
        Text(text = currentUser?.email ?: "Utilisateur inconnu", fontSize = 20.sp, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (watchedMovies.isEmpty() && wantToWatchMovies.isEmpty() && ownedMovies.isEmpty() && wantToSellMovies.isEmpty()) {
                item {
                    Text(text = "Vous n'avez encore ajouté aucun film à votre collection.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (ownedMovies.isNotEmpty()) {
                item { CategoryTitle("📀 Mes films possédés :") }
                items(ownedMovies) { movieTitle ->
                    MovieCard(
                        title = "📀 $movieTitle",
                        onClick = { navController.navigate("movie/$movieTitle/-/-") },
                        onDeleteClick = { removeMovieFromFirebase(movieTitle) }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            if (watchedMovies.isNotEmpty()) {
                item { CategoryTitle("✅ Films vus :") }
                items(watchedMovies) { movieTitle ->
                    MovieCard(
                        title = "✅ $movieTitle",
                        onClick = { navController.navigate("movie/$movieTitle/-/-") },
                        onDeleteClick = { removeMovieFromFirebase(movieTitle) }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            if (wantToWatchMovies.isNotEmpty()) {
                item { CategoryTitle("🎯 À voir :") }
                items(wantToWatchMovies) { movieTitle ->
                    MovieCard(
                        title = "🎯 $movieTitle",
                        onClick = { navController.navigate("movie/$movieTitle/-/-") },
                        onDeleteClick = { removeMovieFromFirebase(movieTitle) }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            if (wantToSellMovies.isNotEmpty()) {
                item { CategoryTitle("💸 À vendre :") }
                items(wantToSellMovies) { movieTitle ->
                    MovieCard(
                        title = "💸 $movieTitle",
                        onClick = { navController.navigate("movie/$movieTitle/-/-") },
                        onDeleteClick = { removeMovieFromFirebase(movieTitle) }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                auth.signOut()
                navController.navigate("auth") { popUpTo(0) }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Se déconnecter")
        }
    }
}

@Composable
fun CategoryTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

// NOUVEAU : Ajout du onClick sur la Card entière !
@Composable
fun MovieCard(title: String, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }, // Rend la carte cliquable
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Supprimer de la liste",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}*/

package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.data.TmdbClient
import kotlinx.coroutines.launch

// Modèle pour associer un titre à son affiche
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

    // Récupère l'URL de l'affiche depuis TMDb par titre
    suspend fun fetchPoster(title: String): String? {
        return try {
            val result = TmdbClient.apiService.searchMovie(myApiKey, title)
            val posterPath = result.results.firstOrNull()?.poster_path
            if (posterPath != null) "https://image.tmdb.org/t/p/w500$posterPath" else null
        } catch (e: Exception) {
            null
        }
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

                    // Pour chaque titre, on récupère l'affiche TMDb
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
        Text(currentUser?.email ?: "Utilisateur inconnu", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
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
                item { ProfileSectionTitle("📀 Mes films possédés") }
                item { MoviePosterGrid(ownedMovies, navController) { removeMovieFromFirebase(it) } }
            }

            if (watchedMovies.isNotEmpty()) {
                item { ProfileSectionTitle("✅ Films vus") }
                item { MoviePosterGrid(watchedMovies, navController) { removeMovieFromFirebase(it) } }
            }

            if (wantToWatchMovies.isNotEmpty()) {
                item { ProfileSectionTitle("🎯 À voir") }
                item { MoviePosterGrid(wantToWatchMovies, navController) { removeMovieFromFirebase(it) } }
            }

            if (wantToSellMovies.isNotEmpty()) {
                item { ProfileSectionTitle("💸 À vendre") }
                item { MoviePosterGrid(wantToSellMovies, navController) { removeMovieFromFirebase(it) } }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                auth.signOut()
                navController.navigate("auth") { popUpTo(0) }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Se déconnecter")
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
    // Grille fixe de 3 colonnes
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
                                        navController.navigate("movie/${movie.title}/-/-")
                                    }
                            )
                            // Bouton supprimer en haut à droite
                            IconButton(
                                onClick = { onDelete(movie.title) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Supprimer",
                                    tint = Color(0xFFE50914),
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