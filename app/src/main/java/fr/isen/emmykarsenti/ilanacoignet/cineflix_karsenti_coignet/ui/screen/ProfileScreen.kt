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

/**
 * Modèle de données simple utilisé pour l'affichage dans le profil.
 * Associe le titre d'un film à l'URL de son affiche (si disponible).
 */
data class MovieWithPoster(
    val title: String,
    val posterUrl: String?
)

@Composable
fun ProfileScreen(navController: NavController) {
    // Initialisation des instances Firebase pour l'authentification et l'utilisateur actuel
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // Scope pour lancer des tâches asynchrones (comme des appels API) dans l'UI
    val coroutineScope = rememberCoroutineScope()
    // Clé API TMDB (idéalement à stocker dans le local.properties pour plus de sécurité)
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"

    // ÉTATS DE L'INTERFACE
    // Ces listes contiennent les films triés par catégorie.
    // "remember { mutableStateOf(...) }" permet à l'interface de se mettre à jour automatiquement
    // lorsque le contenu de ces listes change.
    var watchedMovies by remember { mutableStateOf<List<MovieWithPoster>>(emptyList()) }
    var wantToWatchMovies by remember { mutableStateOf<List<MovieWithPoster>>(emptyList()) }
    var ownedMovies by remember { mutableStateOf<List<MovieWithPoster>>(emptyList()) }
    var wantToSellMovies by remember { mutableStateOf<List<MovieWithPoster>>(emptyList()) }

    /**
     * Fonction asynchrone pour récupérer l'affiche d'un film.
     * 1. Vérifie d'abord si l'affiche est déjà en cache (PosterCache) pour éviter des appels API inutiles.
     * 2. Si non trouvée, lance une recherche sur l'API TMDB avec le titre du film.
     * 3. Récupère le chemin de l'image, construit l'URL complète, la met en cache, et la retourne.
     */
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

    /**
     * Supprime un film de la base de données Firebase Realtime Database de l'utilisateur.
     * Cible le chemin spécifique : userMovies/{userId}/{movieTitle}
     */
    fun removeMovieFromFirebase(movieTitle: String) {
        val uid = currentUser?.uid ?: return
        FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies/$uid/$movieTitle")
            .removeValue() // Supprime le nœud correspondant
    }

    /**
     * LaunchedEffect s'exécute lors du premier affichage de l'écran ou si l'utilisateur change.
     * Met en place un écouteur en temps réel (ValueEventListener) sur la base de données Firebase.
     * À CHAQUE modification (ajout, suppression, modification de statut), ce code est réexécuté.
     */
    LaunchedEffect(currentUser) {
        val uid = currentUser?.uid ?: return@LaunchedEffect
        FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies/$uid")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Listes temporaires pour trier les films reçus de Firebase
                    val tempWatched = mutableListOf<String>()
                    val tempWantToWatch = mutableListOf<String>()
                    val tempOwned = mutableListOf<String>()
                    val tempWantToSell = mutableListOf<String>()

                    // Parcours de chaque film stocké pour cet utilisateur
                    for (movieSnap in snapshot.children) {
                        val title = movieSnap.key ?: continue
                        val wStatus = movieSnap.child("watch_status").getValue(String::class.java)
                        val oStatus = movieSnap.child("own_status").getValue(String::class.java)

                        // Tri selon les statuts enregistrés
                        if (wStatus == "WATCHED") tempWatched.add(title)
                        if (wStatus == "WANT_TO_WATCH") tempWantToWatch.add(title)
                        if (oStatus == "OWN_DVD") tempOwned.add(title)
                        if (oStatus == "WANT_TO_SELL") tempWantToSell.add(title)
                    }

                    // Une fois triés, on lance une coroutine pour récupérer les affiches (appels réseau)
                    // puis on met à jour les états UI. Cela déclenchera la recomposition de l'écran.
                    coroutineScope.launch {
                        watchedMovies = tempWatched.map { MovieWithPoster(it, fetchPoster(it)) }
                        wantToWatchMovies = tempWantToWatch.map { MovieWithPoster(it, fetchPoster(it)) }
                        ownedMovies = tempOwned.map { MovieWithPoster(it, fetchPoster(it)) }
                        wantToSellMovies = tempWantToSell.map { MovieWithPoster(it, fetchPoster(it)) }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // Gestion d'erreur Firebase silencieuse ici (pourrait faire l'objet d'un log)
                }
            })
    }

    // CONSTRUCTION DE L'INTERFACE UTILISATEUR
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1D29))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // En-tête : Titre et informations de l'utilisateur connecté
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

        // LazyColumn permet de créer une liste défilante verticalement.
        // Idéal quand on a beaucoup de contenu (ici, plusieurs grilles de films potentiellement grandes).
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f), // Prend tout l'espace disponible avant le bouton
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Message si l'utilisateur n'a sauvegardé aucun film dans aucune catégorie
            if (watchedMovies.isEmpty() && wantToWatchMovies.isEmpty() && ownedMovies.isEmpty() && wantToSellMovies.isEmpty()) {
                item {
                    Text("Vous n'avez encore ajouté aucun film.", color = Color.Gray)
                }
            }

            // Affichage conditionnel des différentes sections :
            // Si la liste n'est pas vide, on affiche le titre de section puis la grille de films.
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

        // Bouton de déconnexion tout en bas
        Button(
            onClick = {
                auth.signOut() // Déconnexion Firebase
                // Navigation vers l'écran d'authentification et purge de l'historique de navigation
                // pour empêcher l'utilisateur de faire "Retour" et revenir sur son profil déconnecté.
                navController.navigate("auth") { popUpTo(0) }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF299B5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Se déconnecter", fontSize = 14.sp, color = Color.White)
        }
    }
}

/**
 * Composant UI réutilisable pour afficher les titres de sections ("Films vus", "À vendre", etc.).
 */
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

/**
 * Composant affichant une liste de films sous forme de grille (3 colonnes).
 * Utilise la fonction chunked(3) pour découper la liste linéaire en lignes de 3 éléments.
 */
@Composable
fun MoviePosterGrid(
    movies: List<MovieWithPoster>,
    navController: NavController,
    onDelete: (String) -> Unit // Fonction callback déclenchée lors du clic sur la corbeille
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Découpe la liste en sous-listes de 3 éléments maximum
        movies.chunked(3).forEach { rowMovies ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Pour chaque film de la ligne courante
                rowMovies.forEach { movie ->
                    Column(
                        modifier = Modifier.weight(1f), // Partage l'espace équitablement
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Box permet de superposer des éléments (ici l'icône de suppression par-dessus l'affiche)
                        Box {
                            // Affichage asynchrone de l'affiche via Coil
                            AsyncImage(
                                model = movie.posterUrl ?: "",
                                contentDescription = movie.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(2f / 3f) // Format standard des affiches de cinéma
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF31343E)) // Couleur de fond si l'image charge ou est absente
                                    .clickable {
                                        // Lors du clic sur l'affiche :
                                        // 1. Mise en cache de l'affiche pour qu'elle s'affiche instantanément dans l'écran de détails
                                        movie.posterUrl?.let { PosterCache.posters[movie.title] = it }
                                        // 2. Encodage sécurisé du titre (pour gérer les espaces, accents, etc.)
                                        val safeTitre = Uri.encode(movie.title.ifBlank { "Inconnu" })
                                        // 3. Navigation vers MovieDetailScreen avec des paramètres par défaut ("Inconnue", "Profil", etc.)
                                        // car on ne stocke en Firebase que le titre du film, on ne connaît plus son année ou son réalisateur ici.
                                        navController.navigate("movie/$safeTitre/Inconnue/Profil/Inconnue/Inconnu/Pop Culture")
                                    }
                            )
                            // Bouton de suppression (Corbeille) positionné en haut à droite (TopEnd)
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
                        // Titre du film affiché sous l'affiche
                        Text(
                            text = movie.title,
                            color = Color.White,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2, // Limite à 2 lignes
                            overflow = TextOverflow.Ellipsis // Ajoute "..." si le texte est trop long
                        )
                    }
                }
                // Si la dernière ligne a moins de 3 éléments (1 ou 2),
                // on ajoute des espaces vides (Spacer) invisibles pour conserver l'alignement des colonnes.
                repeat(3 - rowMovies.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}