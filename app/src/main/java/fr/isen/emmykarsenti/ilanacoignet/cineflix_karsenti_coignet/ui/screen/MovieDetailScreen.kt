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

// ÉCRAN DE DÉTAIL D'UN FILM
// Cet écran affiche l'affiche, le synopsis, permet à l'utilisateur de gérer
// sa collection (vu, à voir, possédé, à vendre) et d'afficher les vendeurs.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    navController: NavController,
    titre: String,
    annee: String,
    genre: String,
    duree: String,
    realisateur: String
) {
    // Récupération de l'utilisateur actuellement connecté via Firebase Auth
    val currentUser = FirebaseAuth.getInstance().currentUser

    // 1. GESTION DES ÉTATS (Variables qui vont mettre à jour l'interface)

    // Variables pour l'API TMDB (Affiche et Résumé)
    var posterUrl by remember { mutableStateOf<String?>(null) }
    var synopsis by remember { mutableStateOf("Chargement du synopsis...") }
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"

    // Variables pour les statuts Firebase de l'utilisateur connecté
    var watchStatus by remember { mutableStateOf<String?>(null) } // Ex: "WATCHED" ou "WANT_TO_WATCH"
    var ownStatus by remember { mutableStateOf<String?>(null) }   // Ex: "OWN_DVD" ou "WANT_TO_SELL"

    // Liste des pseudonymes des personnes vendant ce film
    var sellers by remember { mutableStateOf<List<String>>(emptyList()) }

    // 2. RÉCUPÉRATION DES DONNÉES DEPUIS TMDB ET FIREBASE

    // A. Récupération de l'affiche et du synopsis (API TMDB)
    // LaunchedEffect(titre) s'exécute une fois à l'ouverture de la page pour ce film
    LaunchedEffect(titre) {
        try {
            // On cherche le film par son titre sur TMDB
            val response = TmdbClient.apiService.searchMovie(myApiKey, titre)

            // Si l'API trouve au moins un résultat
            if (response.results.isNotEmpty()) {
                val movie = response.results[0] // On prend le résultat le plus pertinent (le premier)

                // On construit l'URL de l'image si elle existe
                if (movie.poster_path != null) {
                    posterUrl = "https://image.tmdb.org/t/p/w500${movie.poster_path}"
                }

                // On récupère le synopsis (overview), ou un message par défaut s'il est vide
                if (!movie.overview.isNullOrBlank()) {
                    synopsis = movie.overview
                } else {
                    synopsis = "Aucun synopsis disponible pour ce film."
                }
            } else {
                synopsis = "Film introuvable dans la base de données TMDB."
            }
        } catch (e: Exception) {
            println("Erreur TMDB : ${e.message}")
            synopsis = "Impossible de charger le synopsis (Vérifiez votre connexion)."
        }
    }

    // B. Lecture des statuts de l'utilisateur (Firebase Realtime DB)
    // On écoute en temps réel si l'utilisateur a déjà ce film dans sa collection
    LaunchedEffect(titre) {
        val uid = currentUser?.uid ?: return@LaunchedEffect
        FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies/$uid/$titre")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // On met à jour les variables d'état (ça changera la couleur des boutons)
                    watchStatus = snapshot.child("watch_status").getValue(String::class.java)
                    ownStatus = snapshot.child("own_status").getValue(String::class.java)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // C. Recherche des vendeurs (Aspect Social / Communautaire)
    // On parcourt TOUTE la base de données pour trouver qui a mis "WANT_TO_SELL" sur ce film
    LaunchedEffect(titre) {
        val dbRef = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sellersList = mutableListOf<String>()

                // On boucle sur chaque utilisateur enregistré dans la base
                for (userSnap in snapshot.children) {
                    val userUid = userSnap.key ?: continue

                    // On regarde le statut de CE film spécifique chez CET utilisateur
                    val status = userSnap.child(titre).child("own_status").getValue(String::class.java)

                    // Si l'utilisateur le vend ET que ce n'est pas nous-même
                    if (status == "WANT_TO_SELL" && userUid != currentUser?.uid) {
                        // Pour l'instant on génère un pseudo avec le début de son ID
                        val pseudo = "Utilisateur_" + userUid.take(5)
                        sellersList.add(pseudo)
                    }
                }
                sellers = sellersList // On met à jour la liste affichée à l'écran
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // 3. FONCTIONS D'ACTIONS (Écriture dans Firebase)

    // Met à jour le statut "Visionnage" (Vu / À voir)
    fun setWatchStatus(status: String) {
        val uid = currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies/$uid/$titre/watch_status")

        // Logique "Toggle" : Si on clique sur le bouton déjà actif, ça supprime le statut
        if (watchStatus == status) ref.removeValue() else ref.setValue(status)
    }

    // Met à jour le statut "Possession" (Possédé / À Vendre)
    fun setOwnStatus(status: String) {
        val uid = currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies/$uid/$titre/own_status")

        if (ownStatus == status) ref.removeValue() else ref.setValue(status)
    }

    // 4. INTERFACE GRAPHIQUE (UI)
    Scaffold(
        topBar = {
            // Barre d'en-tête avec bouton retour et titre du film
            TopAppBar(
                title = { Text(titre, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1D29), titleContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF1A1D29) // Fond global sombre
    ) { padding ->

        // LazyColumn permet de faire défiler la page (scroll)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // SECTION 1 : L'Affiche du film
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (posterUrl != null) {
                        AsyncImage(
                            model = posterUrl,
                            contentDescription = titre,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        // Cercle de chargement rouge pendant que TMDB cherche l'image
                        CircularProgressIndicator(color = Color(0xFFE50914))
                    }
                }
            }

            // SECTION 2 : Informations (Titre, Année, Genre, Synopsis)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF31343E))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(titre, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))

                        // Ligne avec les infos de TON JSON !
                        Text("Sortie : $annee  •  Durée : $duree", color = Color.LightGray, fontSize = 14.sp)
                        Text("Genre : $genre", color = Color.LightGray, fontSize = 14.sp)
                        Text("De : $realisateur", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(16.dp))

                        Text("Synopsis", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(synopsis, color = Color.LightGray, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }

            // SECTION 3 : Boutons de la Collection (Seulement si connecté)
            item {
                if (currentUser != null) {
                    // Ligne 1 : Visionnage
                    Text("Mon statut de visionnage", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        StatusBtn("Vu", "WATCHED", watchStatus, Modifier.weight(1f)) { setWatchStatus("WATCHED") }
                        StatusBtn("À voir", "WANT_TO_WATCH", watchStatus, Modifier.weight(1f)) { setWatchStatus("WANT_TO_WATCH") }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Ligne 2 : Possession
                    Text("Ma collection", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        StatusBtn("Je possède", "OWN_DVD", ownStatus, Modifier.weight(1f)) { setOwnStatus("OWN_DVD") }
                        StatusBtn("Vendre", "WANT_TO_SELL", ownStatus, Modifier.weight(1f)) { setOwnStatus("WANT_TO_SELL") }
                    }
                } else {
                    // Message si l'utilisateur navigue en mode "invité"
                    Text("Connectez-vous pour gérer votre collection", color = Color.Gray, fontSize = 14.sp)
                }
            }

            // SECTION 4 : Espace Communautaire (Liste des vendeurs)
            item {
                Spacer(Modifier.height(16.dp))
                Text("Membres cédant ce film", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                if (sellers.isEmpty()) {
                    Text("Aucun utilisateur ne souhaite se séparer de ce film pour le moment.", color = Color.Gray, fontSize = 14.sp)
                }
            }

            // On génère dynamiquement une "Carte" par vendeur trouvé
            items(sellers.size) { index ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C54)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👤", fontSize = 20.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(sellers[index], color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Espace de confort à la fin pour que le dernier item ne soit pas caché par le menu de navigation
            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

// 5. COMPOSANT RÉUTILISABLE : BOUTON DE STATUT
/**
 * Un bouton personnalisé qui change de couleur s'il est actif.
 * @param label Le texte affiché (ex: "Vu")
 * @param status L'identifiant du statut en BDD (ex: "WATCHED")
 * @param currentStatus Le statut actuel sélectionné par l'utilisateur
 * @param onClick L'action à déclencher au clic
 */
@Composable
fun StatusBtn(
    label: String,
    status: String,
    currentStatus: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            // Le bouton devient rouge s'il est sélectionné, sinon il reste bleu nuit
            containerColor = if (currentStatus == status) Color(0xFFE50914) else Color(0xFF2C2C54)
        )
    ) {
        Text(label, fontSize = 12.sp, color = Color.White)
    }
}