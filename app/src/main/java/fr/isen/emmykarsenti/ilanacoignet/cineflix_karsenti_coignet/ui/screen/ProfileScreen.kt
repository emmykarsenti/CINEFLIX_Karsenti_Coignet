package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen

import android.net.Uri // Import nécessaire pour sécuriser le titre du film dans l'URL de navigation
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

//on associe le titre d'un film à l'url de son affiche
data class MovieWithPoster(
    val title: String,
    val posterUrl: String?
)

@Composable
fun ProfileScreen(navController: NavController) {
    // initialisation des instances firebase pour l'authentification et l'utilisateur actuel
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // scope pour lancer des tâches asynchrones comme des appels api
    val coroutineScope = rememberCoroutineScope()
    // clé api tmdb
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"

    //listes de films triées par statut, mises à jour en temps réel depuis firebase
    var watchedMovies by remember { mutableStateOf<List<MovieWithPoster>>(emptyList()) }
    var wantToWatchMovies by remember { mutableStateOf<List<MovieWithPoster>>(emptyList()) }
    var ownedMovies by remember { mutableStateOf<List<MovieWithPoster>>(emptyList()) }
    var wantToSellMovies by remember { mutableStateOf<List<MovieWithPoster>>(emptyList()) }

    //on récupère l'affiche d'un film : d'abord dans le cache local, sinon via l'api tmdb
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

    //on supprime un film de firebase pour le user connecté
    fun removeMovieFromFirebase(movieTitle: String) {
        val uid = currentUser?.uid ?: return
        FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies/$uid/$movieTitle")
            .removeValue() // Supprime le nœud correspondant
    }

    // écoute en temps réel les films du user dans firebase qui se déclenche au chargement de l'écran et à chaque modification dans la base
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

                    // on trie chaque film selon son statut
                    for (movieSnap in snapshot.children) {
                        val title = movieSnap.key ?: continue
                        val wStatus = movieSnap.child("watch_status").getValue(String::class.java)
                        val oStatus = movieSnap.child("own_status").getValue(String::class.java)

                        // tri selon les statuts enregistrés
                        if (wStatus == "WATCHED") tempWatched.add(title)
                        if (wStatus == "WANT_TO_WATCH") tempWantToWatch.add(title)
                        if (oStatus == "OWN_DVD") tempOwned.add(title)
                        if (oStatus == "WANT_TO_SELL") tempWantToSell.add(title)
                    }

                    // on récupère les affiches de chaque film puis on met à jour l'interface
                    coroutineScope.launch {
                        watchedMovies = tempWatched.map { MovieWithPoster(it, fetchPoster(it)) }
                        wantToWatchMovies = tempWantToWatch.map { MovieWithPoster(it, fetchPoster(it)) }
                        ownedMovies = tempOwned.map { MovieWithPoster(it, fetchPoster(it)) }
                        wantToSellMovies = tempWantToSell.map { MovieWithPoster(it, fetchPoster(it)) }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    //interface user
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1D29))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // en-tête avec le titre et l'email de l'utilisateur connecté
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
            modifier = Modifier.fillMaxWidth().weight(1f), // Prend tout l'espace disponible avant le bouton
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // message affiché si l'utilisateur n'a encore rien ajouté
            if (watchedMovies.isEmpty() && wantToWatchMovies.isEmpty() && ownedMovies.isEmpty() && wantToSellMovies.isEmpty()) {
                item {
                    Text("Vous n'avez encore ajouté aucun film.", color = Color.Gray)
                }
            }

            //sections affichées uniquement si elles contiennent des films
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

        //bouton de déconnexion ou coupe la session Firebase et renvoie vers l'écran de connexion
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

//titre de section (ex: "films vus", "à vendre")
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

//grille de 3 colonnes affichant les affiches des films avec un bouton de suppression
@Composable
fun MoviePosterGrid(
    movies: List<MovieWithPoster>,
    navController: NavController,
    onDelete: (String) -> Unit // Fonction callback déclenchée lors du clic sur la corbeille
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // découpe la liste en sous-listes de 3 éléments maximum
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
                        //permet de superposer l'icône poubelle par-dessus l'affiche
                        Box {
                            AsyncImage(
                                model = movie.posterUrl ?: "",
                                contentDescription = movie.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(2f / 3f) // format standard des affiches de cinéma
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF31343E))
                                    .clickable {//on met l'affiche en cache avant de naviguer, pour qu'elle s'affiche instantanément dans MovieDetailScreen
                                        movie.posterUrl?.let { PosterCache.posters[movie.title] = it }
                                        val safeTitre = Uri.encode(movie.title.ifBlank { "Inconnu" })
                                        navController.navigate("movie/$safeTitre/Inconnue/Profil/Inconnue/Inconnu/Pop Culture")
                                    }
                            )
                            // bouton de suppression en haut à droite
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
                        // titre du film affiché sous l'affiche
                        Text(
                            text = movie.title,
                            color = Color.White,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2, // limite à 2 lignes
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // cases vides pour compléter la dernière ligne si elle a moins de 3 films
                repeat(3 - rowMovies.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}