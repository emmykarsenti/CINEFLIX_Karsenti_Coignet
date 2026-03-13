package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.database.*
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.data.TmdbClient

// 1. MODÈLES DE DONNÉES (Structure de la BDD)

// Représente un film tel qu'il est lu dans le JSON Firebase
data class FilmFirebase(
    val titre: String = "",
    val annee: Int = 0,
    val genre: String = "",
    val numero: Int = 0 // Permet de trier chronologiquement (ex: Épisode 1, 2, 3...)
)

// Représente une LIGNE complète sur l'écran (ex: "Animation 2D" et sa liste de films)
data class SousSaga(
    val nom: String = "",
    val films: List<FilmFirebase> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniverseScreen(navController: NavController, universeName: String) {
    // Couleur de fond globale style plateforme de streaming
    val backgroundDark = Color(0xFF1A1D29)

    // 2. PRÉPARATION DES FILTRES DE RECHERCHE

    // On vérifie si l'utilisateur a cliqué sur le bouton global (il peut avoir plusieurs noms selon les tests)
    val isAllCategories = universeName == "Voir tous" || universeName == "Toutes Catégories" || universeName == "All"

    // Le titre affiché tout en haut dans la barre
    val displayTitle = if (isAllCategories) "Toutes catégories" else universeName

    // Traduction pour Firebase : Si on clique sur "Marvel", on cherche "Marvel Cinematic Universe" dans le JSON
    val firebaseName = when (universeName) {
        "Marvel" -> "Marvel Cinematic Universe"
        else -> universeName // Pour Disney, Pixar, Avatar, le nom du bouton = le nom dans le JSON
    }

    // Variables d'état :
    // - sousSagas = nos lignes de films prêtes à être affichées
    // - isLoading = affiche le cercle de chargement rouge le temps de la requête
    var sousSagas by remember { mutableStateOf<List<SousSaga>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 3. RÉCUPÉRATION ET TRI INTELLIGENT (FIREBASE)
    LaunchedEffect(universeName) {
        val db = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app").reference

        // On écoute le nœud "categories" une seule fois
        db.child("categories").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result = mutableListOf<SousSaga>()

                // Niveau 1 : Les Catégories
                for (categorySnap in snapshot.children) {
                    val catName = categorySnap.child("categorie").getValue(String::class.java) ?: ""
                    // Est-ce que le nom de la catégorie contient ce qu'on cherche ? (ex: "Disney")
                    val isCatMatch = catName.contains(firebaseName, ignoreCase = true)

                    val franchisesSnap = categorySnap.child("franchises")

                    // Niveau 2 : Les Franchises
                    for (franchiseSnap in franchisesSnap.children) {
                        val franchiseNom = franchiseSnap.child("nom").getValue(String::class.java) ?: ""
                        val isFranchiseMatch = franchiseNom.contains(firebaseName, ignoreCase = true)

                        // CAS N°1 : LA FRANCHISE CONTIENT DES "SOUS-SAGAS"
                        // C'est le cas de Disney (Animation 2D, Live Action) ou Marvel (Phase 1...)
                        if (franchiseSnap.hasChild("sous_sagas")) {
                            for (ssSnap in franchiseSnap.child("sous_sagas").children) {
                                val ssNom = ssSnap.child("nom").getValue(String::class.java) ?: ""
                                val isSsMatch = ssNom.contains(firebaseName, ignoreCase = true)

                                // Si "Disney" correspond à la catégorie, à la franchise ou à la sous-saga (ou mode "Tout")
                                if (isAllCategories || isCatMatch || isFranchiseMatch || isSsMatch) {
                                    val films = mutableListOf<FilmFirebase>()

                                    // On récupère les films
                                    for (filmSnap in ssSnap.child("films").children) {
                                        val film = FilmFirebase(
                                            titre = filmSnap.child("titre").getValue(String::class.java) ?: "",
                                            annee = filmSnap.child("annee").getValue(Int::class.java) ?: 0,
                                            genre = filmSnap.child("genre").getValue(String::class.java) ?: "",
                                            numero = filmSnap.child("numero").getValue(Int::class.java) ?: 0
                                        )
                                        films.add(film)
                                    }

                                    // Création de la ligne d'affichage
                                    if (films.isNotEmpty()) {
                                        // Le nom de la ligne sera directement celui du JSON (ex: "Animation 2D")
                                        val rowName = if (isAllCategories && franchiseNom.isNotBlank() && franchiseNom != ssNom) {
                                            "$franchiseNom - $ssNom" // Précision utile seulement si on affiche "TOUT"
                                        } else {
                                            ssNom
                                        }
                                        result.add(SousSaga(nom = rowName, films = films.sortedBy { it.numero }))
                                    }
                                }
                            }
                        }

                        // CAS N°2 : LA FRANCHISE CONTIENT DIRECTEMENT DES "FILMS"
                        // C'est le cas de Avatar, Pixar, etc. (Pas de sous-dossiers)
                        if (franchiseSnap.hasChild("films")) {
                            if (isAllCategories || isCatMatch || isFranchiseMatch) {
                                val films = mutableListOf<FilmFirebase>()

                                for (filmSnap in franchiseSnap.child("films").children) {
                                    val film = FilmFirebase(
                                        titre = filmSnap.child("titre").getValue(String::class.java) ?: "",
                                        annee = filmSnap.child("annee").getValue(Int::class.java) ?: 0,
                                        genre = filmSnap.child("genre").getValue(String::class.java) ?: "",
                                        numero = filmSnap.child("numero").getValue(Int::class.java) ?: 0
                                    )
                                    films.add(film)
                                }

                                if (films.isNotEmpty()) {
                                    // La ligne prend le nom de la franchise (ex: "Avatar" ou "Pixar")
                                    val rowName = franchiseNom
                                    result.add(SousSaga(nom = rowName, films = films.sortedBy { it.numero }))
                                }
                            }
                        }
                    }
                }

                // ETAPE FINALE : Fusionner les doublons (si 2 dossiers s'appellent "Animation 2D", on les regroupe)
                val groupedResult = result.groupBy { it.nom }.map { (nom, sagas) ->
                    SousSaga(
                        nom = nom,
                        // distinctBy supprime les films en double, sortedBy les range dans le bon ordre
                        films = sagas.flatMap { it.films }.distinctBy { it.titre }.sortedBy { it.numero }
                    )
                }

                sousSagas = groupedResult
                isLoading = false // Chargement terminé !
            }

            override fun onCancelled(error: DatabaseError) {
                // Si la BDD est injoignable
                isLoading = false
            }
        })
    }

    // 4. INTERFACE UTILISATEUR (Visuel)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(displayTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundDark,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = backgroundDark
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // ÉTAT 1 : En plein chargement
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFFE50914), // Rouge style Netflix
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // ÉTAT 2 : Rien trouvé
            else if (sousSagas.isEmpty()) {
                Text(
                    "Aucun film trouvé pour cet univers",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // ÉTAT 3 : Affichage des lignes de films
            else {
                // Liste verticale scrollable pour afficher toutes les rangées
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(sousSagas) { sousSaga ->
                        Column(modifier = Modifier.fillMaxWidth()) {

                            // LE TITRE DE LA RANGÉE (ex: "Animation 2D" ou "Avatar")
                            Text(
                                text = sousSaga.nom,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)
                            )

                            // LE CARROUSEL HORIZONTAL DES FILMS
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(sousSaga.films) { film ->
                                    // On dessine l'affiche du film
                                    FilmPosterCard(film = film, navController = navController)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. COMPOSANT : LA CARTE D'UN FILM (L'Affiche)
@Composable
fun FilmPosterCard(film: FilmFirebase, navController: NavController) {
    // Stockera le lien de l'image renvoyé par TMDB
    var posterUrl by remember { mutableStateOf<String?>(null) }
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"

    // Au moment de créer la carte, on interroge l'API TMDB avec le titre du film
    LaunchedEffect(film.titre) {
        try {
            val response = TmdbClient.apiService.searchMovie(myApiKey, film.titre)
            // Si on trouve une image, on construit l'URL complète
            if (response.results.isNotEmpty() && response.results[0].poster_path != null) {
                posterUrl = "https://image.tmdb.org/t/p/w500${response.results[0].poster_path}"
            }
        } catch (e: Exception) {
            println("Erreur de chargement de l'image pour ${film.titre}")
        }
    }

    // Le cadre de la carte
    Box(
        modifier = Modifier
            .width(130.dp)
            .height(195.dp) // Ratio standard d'affiche de cinéma
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF31343E)) // Gris foncé en attendant l'image
            .clickable {
                // Redirection vers la page de détails au clic
                navController.navigate("movie/${film.titre}/${film.annee}/${film.genre}")
            },
        contentAlignment = Alignment.Center
    ) {
        // Affiche l'image avec Coil si elle est trouvée
        if (posterUrl != null) {
            AsyncImage(
                model = posterUrl,
                contentDescription = film.titre,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // "Plan B" (Fallback) : Si pas d'image, on affiche le titre en texte
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = film.titre,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${film.annee}", color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}