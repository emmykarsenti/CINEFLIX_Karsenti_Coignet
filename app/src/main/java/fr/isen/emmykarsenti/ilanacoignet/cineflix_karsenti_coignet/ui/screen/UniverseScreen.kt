package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen

import android.net.Uri
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

// 1. MODÈLES DE DONNÉES (Structure alignée sur la base Firebase)

// Représente un film tel qu'il est stocké dans notre base de données Firebase.
// On utilise des valeurs par défaut pour éviter les crashs si un champ est manquant.
data class FilmFirebase(
    val titre: String = "",
    val annee: Int = 0,
    val genre: String = "",
    val numero: Int = 0, // Utilisé pour trier les films dans l'ordre chronologique
    val duree: String = "",
    val realisateur: String = ""
)

// Représente une ligne complète de films à l'écran (ex: "Phase 1", "Animation 2D").
// Contient le titre de la rangée et la liste de ses films.
data class SousSaga(
    val nom: String = "",
    val films: List<FilmFirebase> = emptyList()
)

// 2. ÉCRAN PRINCIPAL : AFFICHAGE DE L'UNIVERS (Disney, Marvel, etc.)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniverseScreen(navController: NavController, universeName: String) {
    // Charte graphique : Couleur de fond sombre style Netflix / Disney+
    val backgroundDark = Color(0xFF1A1D29)

    // Détermine si l'utilisateur a cliqué sur le bouton "Toutes les catégories"
    val isAllCategories = universeName == "Voir tous" || universeName == "Toutes Catégories" || universeName == "All"

    // Titre affiché dans la barre supérieure de l'application
    val displayTitle = if (isAllCategories) "Toutes catégories" else universeName

    // Adaptation du nom pour coller exactement à la structure JSON dans Firebase
    val firebaseName = when (universeName) {
        "Marvel" -> "Marvel Cinematic Universe"
        else -> universeName // Pour Disney, Pixar, etc., le nom reste identique
    }

    // GESTION DE L'ÉTAT
    // sousSagas : Stocke les listes de films triées par catégories prêtes à être affichées
    var sousSagas by remember { mutableStateOf<List<SousSaga>>(emptyList()) }
    // isLoading : Contrôle l'affichage de l'animation de chargement
    var isLoading by remember { mutableStateOf(true) }

    // 3. RÉCUPÉRATION ET TRAITEMENT DES DONNÉES DEPUIS FIREBASE
    // Ce bloc se lance une seule fois quand l'écran s'ouvre (ou si l'univers change)
    LaunchedEffect(universeName) {
        // Connexion à la base de données Firebase
        val db = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app").reference

        // On lit le nœud "categories" en une seule passe
        db.child("categories").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result = mutableListOf<SousSaga>()

                // Parcours des catégories (Niveau 1)
                for (categorySnap in snapshot.children) {
                    val catName = categorySnap.child("categorie").getValue(String::class.java) ?: ""
                    val isCatMatch = catName.contains(firebaseName, ignoreCase = true)

                    val franchisesSnap = categorySnap.child("franchises")

                    // Parcours des franchises (Niveau 2)
                    for (franchiseSnap in franchisesSnap.children) {
                        val franchiseNom = franchiseSnap.child("nom").getValue(String::class.java) ?: ""
                        val isFranchiseMatch = franchiseNom.contains(firebaseName, ignoreCase = true)

                        // CAS A : La franchise contient des sous-catégories (ex: Disney -> Animation 2D)
                        if (franchiseSnap.hasChild("sous_sagas")) {
                            for (ssSnap in franchiseSnap.child("sous_sagas").children) {
                                val ssNom = ssSnap.child("nom").getValue(String::class.java) ?: ""
                                val isSsMatch = ssNom.contains(firebaseName, ignoreCase = true)

                                // Si ça correspond à notre recherche (ou si on veut "Tout" voir)
                                if (isAllCategories || isCatMatch || isFranchiseMatch || isSsMatch) {
                                    val films = mutableListOf<FilmFirebase>()

                                    // Parcours des films de cette sous-saga
                                    for (filmSnap in ssSnap.child("films").children) {
                                        // FIX IMPORTANT : Gérer l'absence de "genre" dans le JSON
                                        val genreBDD = filmSnap.child("genre").getValue(String::class.java)
                                        // Si le genre est vide dans la BDD, on utilise le nom de l'univers actuel par défaut
                                        val finalGenre = if (!genreBDD.isNullOrBlank()) genreBDD else universeName

                                        val film = FilmFirebase(
                                            titre = filmSnap.child("titre").getValue(String::class.java) ?: "",
                                            annee = filmSnap.child("annee").getValue(Int::class.java) ?: 0,
                                            genre = finalGenre,
                                            numero = filmSnap.child("numero").getValue(Int::class.java) ?: 0,
                                            duree = filmSnap.child("duree").getValue(String::class.java) ?: "Inconnue",
                                            realisateur = filmSnap.child("realisateur").getValue(String::class.java) ?: "Inconnu"
                                        )
                                        films.add(film)
                                    }

                                    // Si on a trouvé des films, on crée la rangée (SousSaga)
                                    if (films.isNotEmpty()) {
                                        // Formatage intelligent du titre de la rangée pour éviter les ambiguïtés
                                        val rowName = if (isAllCategories && franchiseNom.isNotBlank() && franchiseNom != ssNom) {
                                            "$franchiseNom - $ssNom" // Précise la franchise globale si on affiche tout
                                        } else {
                                            ssNom
                                        }
                                        // On ajoute la ligne en triant les films par ordre chronologique
                                        result.add(SousSaga(nom = rowName, films = films.sortedBy { it.numero }))
                                    }
                                }
                            }
                        }

                        // CAS B : La franchise contient directement des films (ex: Avatar, Pixar)
                        if (franchiseSnap.hasChild("films")) {
                            if (isAllCategories || isCatMatch || isFranchiseMatch) {
                                val films = mutableListOf<FilmFirebase>()

                                for (filmSnap in franchiseSnap.child("films").children) {
                                    // Même vérification vitale pour le genre
                                    val genreBDD = filmSnap.child("genre").getValue(String::class.java)
                                    val finalGenre = if (!genreBDD.isNullOrBlank()) genreBDD else universeName

                                    val film = FilmFirebase(
                                        titre = filmSnap.child("titre").getValue(String::class.java) ?: "",
                                        annee = filmSnap.child("annee").getValue(Int::class.java) ?: 0,
                                        genre = finalGenre,
                                        numero = filmSnap.child("numero").getValue(Int::class.java) ?: 0
                                    )
                                    films.add(film)
                                }

                                if (films.isNotEmpty()) {
                                    val rowName = franchiseNom
                                    result.add(SousSaga(nom = rowName, films = films.sortedBy { it.numero }))
                                }
                            }
                        }
                    }
                }

                // NETTOYAGE FINAL DES DONNÉES
                // On regroupe les rangées ayant le même nom et on supprime les doublons potentiels de films
                val groupedResult = result.groupBy { it.nom }.map { (nom, sagas) ->
                    SousSaga(
                        nom = nom,
                        films = sagas.flatMap { it.films }.distinctBy { it.titre }.sortedBy { it.numero }
                    )
                }

                sousSagas = groupedResult
                isLoading = false // Fin du chargement
            }

            override fun onCancelled(error: DatabaseError) {
                // Gestion de l'erreur (si problème de connexion, on arrête de charger et on affiche vide)
                isLoading = false
            }
        })
    }

    // 4. INTERFACE UTILISATEUR (UI)
    Scaffold(
        topBar = {
            // Barre de navigation supérieure (Titre + Bouton Retour)
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

            // AFFICHAGES CONDITIONNELS

            // ÉTAT 1 : Chargement en cours
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFFE50914), // Rouge vif
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // ÉTAT 2 : Aucun film trouvé
            else if (sousSagas.isEmpty()) {
                Text(
                    text = "Aucun film trouvé pour cet univers",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // ÉTAT 3 : Affichage des listes de films
            else {
                // LazyColumn = Liste verticale (pour afficher les différentes catégories/sagas)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(sousSagas) { sousSaga ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Titre de la rangée (ex: "La Renaissance Disney")
                            Text(
                                text = sousSaga.nom,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)
                            )

                            // LazyRow = Carrousel horizontal (pour scroller les films d'une même saga)
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(sousSaga.films) { film ->
                                    // Appel du composant qui dessine l'affiche d'un film
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

// 5. COMPOSANT : CARTE INDIVIDUELLE D'UN FILM (Affiche TMDB)
@Composable
fun FilmPosterCard(film: FilmFirebase, navController: NavController) {
    // État pour stocker l'URL de l'image renvoyée par l'API TMDB
    var posterUrl by remember { mutableStateOf<String?>(null) }
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"

    // Requête API vers TMDB pour récupérer l'affiche du film en utilisant son titre
    LaunchedEffect(film.titre) {
        try {
            val response = TmdbClient.apiService.searchMovie(myApiKey, film.titre)
            // Si l'API trouve le film et qu'il a une affiche, on construit l'URL complète
            if (response.results.isNotEmpty() && response.results[0].poster_path != null) {
                posterUrl = "https://image.tmdb.org/t/p/w500${response.results[0].poster_path}"
            }
        } catch (e: Exception) {
            println("Erreur de chargement de l'image pour ${film.titre}")
        }
    }

    // Conteneur de la carte du film
    Box(
        modifier = Modifier
            .width(130.dp)
            .height(195.dp) // Ratio 2:3 classique pour les affiches de cinéma
            .clip(RoundedCornerShape(8.dp)) // Bords arrondis
            .background(Color(0xFF31343E)) // Couleur de fond "placeholder" (en attendant l'image)
            .clickable {
                // On encode les textes (Uri.encode) pour éviter que des espaces ou des caractères
                // spéciaux (comme dans "Lilo & Stitch") ne fassent planter la route Compose.
                val safeTitre = Uri.encode(if (film.titre.isNotBlank()) film.titre else "Inconnu")
                val safeGenre = Uri.encode(if (film.genre.isNotBlank()) film.genre else "Inconnu")
                val safeDuree = Uri.encode(if (film.duree.isNotBlank()) film.duree else "Inconnue")
                val safeRealisateur = Uri.encode(if (film.realisateur.isNotBlank()) film.realisateur else "Inconnu")

                navController.navigate("movie/$safeTitre/${film.annee}/$safeGenre/$safeDuree/$safeRealisateur")
            },
        contentAlignment = Alignment.Center
    ) {
        // Affichage de l'affiche si elle a été trouvée par TMDB
        if (posterUrl != null) {
            AsyncImage(
                model = posterUrl,
                contentDescription = film.titre,
                contentScale = ContentScale.Crop, // Remplit tout le cadre sans déformer l'image
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Si l'API TMDB ne trouve pas d'image, on affiche le titre et l'année en format texte
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