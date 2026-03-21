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

//représente un film mis en vente par un autre user
data class FilmEnVente(
    val titre: String,
    val vendeurPseudo: String,
    val posterUrl: String?
)

@Composable
fun MarketScreen(navController: NavController) {
    //// on exclut les films du user connecté de la liste d'achats
    val currentUser = FirebaseAuth.getInstance().currentUser
    val coroutineScope = rememberCoroutineScope()
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"
    val prixDefaut = "5€" // prix fixe affiché pour tous les films

    // ÉTATS DE L'INTERFACE
    // Liste des films disponibles à la vente sur la plateforme
    var filmsEnVente by remember { mutableStateOf<List<FilmEnVente>>(emptyList()) }
    // Gère l'affichage du logo de chargement pendant la requête Firebase
    var isLoading by remember { mutableStateOf(true) }

    // // récupère l'affiche d'un film : depuis le cache si disponible, sinon via tmdb
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

    LaunchedEffect(Unit) {
        val db = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")

        //on écoute tous les films de tous les utilisateurs en temps réel
        db.getReference("userMovies")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val result = mutableListOf<FilmEnVente>()

                    //compteur pour savoir quand toutes les requêtes de pseudos sont terminées
                    var usersRestants = snapshot.children.count { it.key != currentUser?.uid }

                    // si personne d'autre n'a de films on arrête le chargement
                    if (usersRestants == 0) {
                        isLoading = false
                        return
                    }

                    //on parcourt chaque user et ses films
                    for (userSnap in snapshot.children) {
                        val uid = userSnap.key ?: continue
                        if (uid == currentUser?.uid) continue //// on ignore nos propres films

                        //pour chaque user, on récupère son pseudo dans firebase
                        db.getReference("users/$uid/username")
                            .get()
                            .addOnSuccessListener { usernameSnap ->
                                val pseudo = usernameSnap.getValue(String::class.java) // // si pas de pseudo enregistré, on en génère un par défaut
                                    ?: "Utilisateur_${uid.take(5)}"

                                //on ajoute à la liste les films que ce user souhaite vendre
                                for (filmSnap in userSnap.children) {
                                    val titre = filmSnap.key ?: continue
                                    val ownStatus = filmSnap.child("own_status").getValue(String::class.java)

                                    // si le film a le statut want to sell, on l'ajoute à la liste de résultats
                                    if (ownStatus == "WANT_TO_SELL") {
                                        result.add(FilmEnVente(titre, pseudo, null))
                                    }
                                }

                                usersRestants--

                                if (usersRestants == 0) { // quand tous les users ont été traités, on charge les affiches et on affiche
                                    coroutineScope.launch {
                                        filmsEnVente = result.map { it.copy(posterUrl = fetchPoster(it.titre)) } // recuperation des affiches de film
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

    //interface user
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1D29))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // en-tête
        Text("Achat/Revente", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF299B5))
        Text("Films proposés par la communauté CinéFlix", fontSize = 14.sp, color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp))

        // 3 états possibles: chargement / vide / liste remplie
        if (isLoading) {//chargement
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFF299B5))
            }
        } else if (filmsEnVente.isEmpty()) { //vide: aucun film à vendre
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun film en vente pour le moment.", color = Color.Gray)
            }
        } else {//pour liste remplie
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filmsEnVente) { film ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF31343E))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            //affiche film
                            AsyncImage(
                                model = film.posterUrl ?: "",
                                contentDescription = film.titre,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1A1D29))
                            )
                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(film.titre, color = Color.White, fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(4.dp))

                                // pseudo du vendeur
                                Text("👤 ${film.vendeurPseudo}", color = Color.Gray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))

                                // prix film
                                Text(" $prixDefaut", color = Color(0xFFF299B5), fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                //bouton de contact du vendeur
                                Button(
                                    onClick = { },
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
                //espace en bas pour ne pas être caché par la barre de navigation
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}