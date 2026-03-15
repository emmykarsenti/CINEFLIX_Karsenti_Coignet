package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.data.TmdbClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    navController: NavController,
    titre: String,
    annee: String,
    genre: String,
    duree: String,
    realisateur: String,
    franchise: String
) {
    // Récupération de l'utilisateur actuellement connecté via Firebase
    val currentUser = FirebaseAuth.getInstance().currentUser

    // 1. VARIABLES D'ÉTAT (State)
    var posterUrl by remember { mutableStateOf<String?>(null) }
    var synopsis by remember { mutableStateOf("Chargement du synopsis...") }
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"

    // Variables d'affichage : elles prennent les valeurs par défaut reçues en paramètre,
    // mais pourront être écrasées par les vraies données de TMDB.
    var displayTitre by remember { mutableStateOf(titre) }
    var displayFranchise by remember { mutableStateOf(franchise) }
    var displayGenre by remember { mutableStateOf(genre) }
    var displayDuree by remember { mutableStateOf(duree) }
    var displayRealisateur by remember { mutableStateOf(realisateur) }

    // Statuts de l'utilisateur pour ce film
    var watchStatus by remember { mutableStateOf<String?>(null) } // Vu / À voir
    var ownStatus by remember { mutableStateOf<String?>(null) }   // Possède / Veut vendre
    var sellers by remember { mutableStateOf<List<String>>(emptyList()) } // Liste des vendeurs

    // 2. RÉCUPÉRATION DES DONNÉES TMDB
    LaunchedEffect(titre) {
        try {
            // Recherche du film sur TMDB avec le titre fourni
            val response = TmdbClient.apiService.searchMovie(myApiKey, titre)

            if (response.results.isNotEmpty()) {
                val movie = response.results[0]

                // Récupération de l'affiche et du synopsis
                if (movie.poster_path != null) {
                    posterUrl = "https://image.tmdb.org/t/p/w500${movie.poster_path}"
                }
                synopsis = if (!movie.overview.isNullOrBlank()) movie.overview else "Aucun synopsis disponible pour ce film."

                // Récupération des détails avancés (en français) via une fonction personnalisée
                try {
                    val movieId = movie.id
                    val extraData = fetchTmdbDetails(movieId, myApiKey)

                    // Mise à jour des textes si TMDB a trouvé de meilleures informations
                    if (extraData["title"]?.isNotBlank() == true) displayTitre = extraData["title"]!!
                    if (extraData["genre"]?.isNotBlank() == true) displayGenre = extraData["genre"]!!
                    if (extraData["runtime"]?.isNotBlank() == true) displayDuree = extraData["runtime"]!!
                    if (extraData["director"]?.isNotBlank() == true) displayRealisateur = extraData["director"]!!

                    val tmdbFranchise = extraData["franchise"] ?: ""

                    // Gestion de l'affichage du texte rouge (Saga / Franchise / Studio)
                    if (tmdbFranchise.isNotBlank()) {
                        displayFranchise = tmdbFranchise
                    } else {
                        // Si TMDB n'a pas de saga et que la catégorie actuelle est trop générique, on la masque.
                        if (displayFranchise.equals("Pop Culture", ignoreCase = true) ||
                            displayFranchise == "Inconnue" ||
                            displayFranchise == "Nouveauté" ||
                            displayFranchise == "Populaire") {
                            displayFranchise = ""
                        }
                    }

                } catch (e: Exception) {
                    println("Détails supplémentaires introuvables : ${e.message}")
                }

            } else {
                synopsis = "Film introuvable dans la base de données TMDB."
            }
        } catch (e: Exception) {
            synopsis = "Impossible de charger les données (Vérifiez votre connexion)."
        }
    }

    // 3. LECTURE DES STATUTS FIREBASE (VISIONNAGE & POSSESSION)
    LaunchedEffect(titre) {
        val uid = currentUser?.uid ?: return@LaunchedEffect
        FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies/$uid/$titre")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    watchStatus = snapshot.child("watch_status").getValue(String::class.java)
                    ownStatus = snapshot.child("own_status").getValue(String::class.java)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // 4. LECTURE DES VENDEURS FIREBASE
    LaunchedEffect(titre) {
        val dbRef = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sellersList = mutableListOf<String>()
                // On parcourt tous les utilisateurs pour voir qui possède ce film avec le statut WANT_TO_SELL
                for (userSnap in snapshot.children) {
                    val userUid = userSnap.key ?: continue
                    val status = userSnap.child(titre).child("own_status").getValue(String::class.java)
                    if (status == "WANT_TO_SELL" && userUid != currentUser?.uid) {
                        sellersList.add("Utilisateur_" + userUid.take(5)) // On n'affiche que le début de l'UID pour l'anonymat
                    }
                }
                sellers = sellersList
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Fonctions pour mettre à jour les statuts dans Firebase lors d'un clic
    fun setWatchStatus(status: String) {
        val uid = currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app").getReference("userMovies/$uid/$titre/watch_status")
        if (watchStatus == status) ref.removeValue() else ref.setValue(status)
    }

    fun setOwnStatus(status: String) {
        val uid = currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app").getReference("userMovies/$uid/$titre/own_status")
        if (ownStatus == status) ref.removeValue() else ref.setValue(status)
    }

    // 5. INTERFACE UTILISATEUR (UI)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(displayTitre, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Bouton retour
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1D29), titleContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF1A1D29)
    ) { padding ->

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Affiche du film
            item {
                Box(modifier = Modifier.fillMaxWidth().height(350.dp), contentAlignment = Alignment.Center) {
                    if (posterUrl != null) {
                        AsyncImage(model = posterUrl, contentDescription = displayTitre, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Fit)
                    } else {
                        CircularProgressIndicator(color = Color(0xFFE50914))
                    }
                }
            }

            // Fiche détaillée (Titre, Saga, Info, Synopsis)
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF31343E))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(displayTitre, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

                        // Affichage du texte rouge (Franchise/Studio) s'il y en a un
                        if (displayFranchise != "Inconnue" && displayFranchise.isNotBlank()) {
                            Text(displayFranchise.uppercase(), color = Color(0xFFE50914), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        }

                        Spacer(Modifier.height(8.dp))

                        // Informations générales
                        if (annee != "Inconnue" && annee != "-") Text("📅 Sortie : $annee", color = Color.LightGray, fontSize = 14.sp)
                        if (displayGenre != "Inconnu" && displayGenre != "-" && displayGenre != "Nouveauté" && displayGenre != "Populaire") Text("🎬 Genre : $displayGenre", color = Color.LightGray, fontSize = 14.sp)
                        if (displayDuree != "Inconnue" && displayDuree != "-") Text("⏱ Durée : $displayDuree", color = Color.LightGray, fontSize = 14.sp)
                        if (displayRealisateur != "Inconnu" && displayRealisateur != "-") Text("🎥 De : $displayRealisateur", color = Color.White, fontSize = 14.sp)

                        Spacer(Modifier.height(16.dp))

                        // Synopsis
                        Text("Synopsis", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(synopsis, color = Color.LightGray, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }

            // Boutons d'action (Si l'utilisateur est connecté)
            item {
                if (currentUser != null) {
                    Text("Mon statut de visionnage", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        StatusBtn("Vu", "WATCHED", watchStatus, Modifier.weight(1f)) { setWatchStatus("WATCHED") }
                        StatusBtn("À voir", "WANT_TO_WATCH", watchStatus, Modifier.weight(1f)) { setWatchStatus("WANT_TO_WATCH") }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("Ma collection", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        StatusBtn("Je possède", "OWN_DVD", ownStatus, Modifier.weight(1f)) { setOwnStatus("OWN_DVD") }
                        StatusBtn("Vendre", "WANT_TO_SELL", ownStatus, Modifier.weight(1f)) { setOwnStatus("WANT_TO_SELL") }
                    }
                } else {
                    Text("Connectez-vous pour gérer votre collection", color = Color.Gray, fontSize = 14.sp)
                }
            }

            // Section Marché d'occasion (Utilisateurs vendant ce film)
            item {
                Spacer(Modifier.height(16.dp))
                Text("Membres cédant ce film", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                if (sellers.isEmpty()) Text("Aucun utilisateur ne souhaite se séparer de ce film pour le moment.", color = Color.Gray, fontSize = 14.sp)
            }

            items(sellers.size) { index ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C54)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("👤", fontSize = 20.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(sellers[index], color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// Composant réutilisable pour les boutons de statut (rouge si sélectionné, gris sinon)
@Composable
fun StatusBtn(label: String, status: String, currentStatus: String?, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = if (currentStatus == status) Color(0xFFE50914) else Color(0xFF2C2C54))) {
        Text(label, fontSize = 12.sp, color = Color.White)
    }
}

// 6. FONCTION DE RÉCUPÉRATION AVANCÉE TMDB
suspend fun fetchTmdbDetails(movieId: Int, apiKey: String): Map<String, String> {
    return withContext(Dispatchers.IO) { // S'exécute sur un thread de fond (IO) pour ne pas bloquer l'UI
        try {
            val url = "https://api.themoviedb.org/3/movie/$movieId?api_key=$apiKey&language=fr-FR&append_to_response=credits"
            val responseString = URL(url).readText()
            val json = JSONObject(responseString)

            val foundTitle = json.optString("title") // Vrai titre en VF

            // RECHERCHE DE LA FRANCHISE / STUDIO
            var foundFranchise = ""
            val prodCompanies = json.optJSONArray("production_companies")
            var isMarvel = false
            var isDisney = false
            var isPixar = false
            var isDC = false
            var isStarWars = false

            // On boucle sur les studios de production pour détecter les grands univers
            if (prodCompanies != null) {
                for (i in 0 until prodCompanies.length()) {
                    val comp = prodCompanies.getJSONObject(i).optString("name").lowercase()
                    if (comp.contains("marvel")) isMarvel = true
                    if (comp.contains("pixar")) isPixar = true
                    if (comp.contains("disney")) isDisney = true
                    if (comp.contains("dc comics") || comp.contains("dc entertainment")) isDC = true
                    if (comp.contains("lucasfilm")) isStarWars = true
                }
            }

            // On vérifie s'il y a une "Collection" officielle (ex: Avatar Collection)
            var tmdbCollection = ""
            if (json.has("belongs_to_collection") && !json.isNull("belongs_to_collection")) {
                tmdbCollection = json.getJSONObject("belongs_to_collection").optString("name")
                    .replace(" Collection", "")
                    .replace(" - Saga", "")
                    .replace(" Saga", "")
                    .trim()
                // Nettoyage des articles pour faire plus propre
                if (tmdbCollection.startsWith("The ")) tmdbCollection = tmdbCollection.substring(4)
                if (tmdbCollection.startsWith("Les ")) tmdbCollection = tmdbCollection.substring(4)
            }

            // Application des règles de priorité (Pixar gagne sur Disney, Marvel gagne sur Saga Avengers, etc.)
            if (isMarvel) {
                foundFranchise = "Marvel"
            } else if (isStarWars) {
                foundFranchise = "Star Wars"
            } else if (isPixar) {
                foundFranchise = "Pixar"
            } else if (isDisney) {
                foundFranchise = "Disney"
            } else if (isDC) {
                foundFranchise = "DC Comics"
            } else if (tmdbCollection.isNotEmpty()) {
                foundFranchise = tmdbCollection
            }

            // CALCUL DE LA DURÉE (Minutes -> Heures / Minutes)
            var foundRuntime = ""
            if (json.has("runtime") && !json.isNull("runtime")) {
                val r = json.getInt("runtime")
                if (r > 0) {
                    val h = r / 60
                    val m = r % 60
                    foundRuntime = if (h > 0) "${h}h ${m}min" else "${m}min"
                }
            }

            // LISTE DES GENRES
            var foundGenres = ""
            val genresArr = json.optJSONArray("genres")
            if (genresArr != null && genresArr.length() > 0) {
                val list = mutableListOf<String>()
                for (i in 0 until genresArr.length()) {
                    list.add(genresArr.getJSONObject(i).optString("name"))
                }
                foundGenres = list.joinToString(", ")
            }

            // RECHERCHE DU RÉALISATEUR
            var foundDirector = ""
            val credits = json.optJSONObject("credits")
            if (credits != null) {
                val crew = credits.optJSONArray("crew")
                if (crew != null) {
                    for (i in 0 until crew.length()) {
                        val member = crew.getJSONObject(i)
                        if (member.optString("job") == "Director") {
                            foundDirector = member.optString("name")
                            break
                        }
                    }
                }
            }

            // On retourne tous les éléments trouvés sous forme de dictionnaire (Map)
            mapOf(
                "title" to foundTitle,
                "franchise" to foundFranchise,
                "runtime" to foundRuntime,
                "genre" to foundGenres,
                "director" to foundDirector
            )
        } catch (e: Exception) {
            emptyMap()
        }
    }
}