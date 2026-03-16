/* package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen

... (Ancienne version du code conservée telle quelle) ...

}*/

package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
 * Modèle de données pour un film mis en vente par un utilisateur.
 * Contient le titre, le pseudo du vendeur et l'URL de l'affiche.
 */
data class FilmEnVente(
    val titre: String,
    val vendeurPseudo: String,
    val posterUrl: String?
)

@Composable
fun MarketScreen(navController: NavController) {
    // Récupération de l'utilisateur actuellement connecté pour exclure ses propres films de la liste d'achat
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Scope pour lancer des requêtes asynchrones (comme la récupération des affiches)
    val coroutineScope = rememberCoroutineScope()
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"

    // Prix par défaut affiché pour tous les films (pourrait être dynamisé plus tard)
    val prixDefaut = "5€"

    // ÉTATS DE L'INTERFACE
    // Liste des films disponibles à la vente sur la plateforme
    var filmsEnVente by remember { mutableStateOf<List<FilmEnVente>>(emptyList()) }
    // Gère l'affichage du logo de chargement pendant la requête Firebase
    var isLoading by remember { mutableStateOf(true) }

    /**
     * Fonction pour récupérer l'URL de l'affiche d'un film.
     * Utilise le système de cache pour éviter de spammer l'API TMDB si l'image a déjà été chargée ailleurs.
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

    // NOUVELLE VERSION DU CHARGEMENT AVEC RÉCUPÉRATION DES PSEUDOS
    LaunchedEffect(Unit) {
        val db = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")

        // 1. On écoute le nœud global "userMovies" pour voir tous les films de tous les utilisateurs
        db.getReference("userMovies")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val result = mutableListOf<FilmEnVente>()

                    // On compte le nombre d'utilisateurs restants à traiter (en excluant l'utilisateur actuel)
                    // Cela nous permettra de savoir quand toutes les requêtes de pseudos seront terminées
                    var usersRestants = snapshot.children.count { it.key != currentUser?.uid }

                    // Si personne d'autre n'a de films, on arrête le chargement immédiatement
                    if (usersRestants == 0) {
                        isLoading = false
                        return
                    }

                    // 2. On parcourt chaque utilisateur et ses films
                    for (userSnap in snapshot.children) {
                        val uid = userSnap.key ?: continue

                        // On ignore nos propres films (on ne peut pas s'acheter un film à soi-même)
                        if (uid == currentUser?.uid) continue

                        // 3. Pour chaque utilisateur, on fait une requête asynchrone pour récupérer son pseudo
                        db.getReference("users/$uid/username")
                            .get()
                            .addOnSuccessListener { usernameSnap ->
                                // Si le pseudo n'existe pas, on génère un pseudo par défaut avec le début de l'UID
                                val pseudo = usernameSnap.getValue(String::class.java)
                                    ?: "Utilisateur_${uid.take(5)}"

                                // 4. On parcourt les films de cet utilisateur précis
                                for (filmSnap in userSnap.children) {
                                    val titre = filmSnap.key ?: continue
                                    val ownStatus = filmSnap.child("own_status").getValue(String::class.java)

                                    // Si le film a le statut "WANT_TO_SELL" (À vendre), on l'ajoute à la liste de résultats
                                    if (ownStatus == "WANT_TO_SELL") {
                                        result.add(FilmEnVente(titre, pseudo, null))
                                    }
                                }

                                // 5. On décrémente le compteur d'utilisateurs traités
                                usersRestants--

                                // Si tous les utilisateurs ont été traités, on peut finaliser
                                if (usersRestants == 0) {
                                    coroutineScope.launch {
                                        // On récupère les affiches pour tous les films trouvés
                                        filmsEnVente = result.map { it.copy(posterUrl = fetchPoster(it.titre)) }
                                        // On désactive l'écran de chargement
                                        isLoading = false
                                    }
                                }
                            }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    isLoading = false
                }
            })
    }

    // CONSTRUCTION DE L'INTERFACE UTILISATEUR
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1D29)) // Fond global
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // En-tête de la page Market
        Text("Achat/Revente", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF299B5))
        Text("Films proposés par la communauté CinéFlix", fontSize = 14.sp, color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp))

        // Gestion des 3 états de l'écran : Chargement / Vide / Liste remplie
        if (isLoading) {
            // ÉTAT 1 : Chargement en cours
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFF299B5))
            }
        } else if (filmsEnVente.isEmpty()) {
            // ÉTAT 2 : Aucun film à vendre trouvé
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun film en vente pour le moment.", color = Color.Gray)
            }
        } else {
            // ÉTAT 3 : Affichage de la liste des films
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filmsEnVente) { film ->
                    // Carte individuelle pour chaque film
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF31343E))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Affiche du film
                            AsyncImage(
                                model = film.posterUrl ?: "",
                                contentDescription = film.titre,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1A1D29)) // Fond noir si l'image est manquante
                            )
                            Spacer(modifier = Modifier.width(12.dp))

                            // Informations sur la droite de l'affiche
                            Column(modifier = Modifier.weight(1f)) {
                                Text(film.titre, color = Color.White, fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(4.dp))

                                // Pseudo du vendeur
                                Text("👤 ${film.vendeurPseudo}", color = Color.Gray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))

                                // Prix du film
                                Text(" $prixDefaut", color = Color(0xFFF299B5), fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Bouton de contact (logique de messagerie ou email à implémenter)
                                Button(
                                    onClick = { /* Action à définir : ouvrir un chat ou envoyer un email */ },
                                    modifier = Modifier.height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C54))
                                ) {
                                    Text("Contacter le vendeur", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
                // Espace vide en bas pour éviter que le dernier élément ne soit caché par la barre de navigation
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}