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

//représente un film tel qu'il est stocké dans firebase
data class FilmFirebase(
    val titre: String = "",
    val annee: Int = 0,
    val genre: String = "",
    val numero: Int = 0, // numéro d'ordre dans la saga (pour le tri)
    val duree: String = "",
    val realisateur: String = "",
    val franchise: String = "" //franchise sauvegardée pour l'écran de détails
)

data class SousSaga(
    val nom: String = "",
    val films: List<FilmFirebase> = emptyList()
)

//écran principal
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniverseScreen(navController: NavController, universeName: String) {
    val backgroundDark = Color(0xFF1A1D29)
    //// "voir tous" affiche toutes les franchises sans filtre
    val isAllCategories = universeName == "Voir tous" || universeName == "Toutes Catégories" || universeName == "All"
    val displayTitle = if (isAllCategories) "Toutes catégories" else universeName

    //certains noms d'univers dans firebase diffèrent de ceux affichés dans l'ui
    val firebaseName = when (universeName) {
        "Marvel" -> "Marvel Cinematic Universe"
        else -> universeName
    }

    var sousSagas by remember { mutableStateOf<List<SousSaga>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    //chargement des films depuis Firebase au lancement de l'écran
    LaunchedEffect(universeName) {
        val db = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app").reference

        //écoute du dossier entier "categories"
        db.child("categories").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result = mutableListOf<SousSaga>()

                for (categorySnap in snapshot.children) {
                    val catName = categorySnap.child("categorie").getValue(String::class.java) ?: ""
                    val isCatMatch = catName.contains(firebaseName, ignoreCase = true)
                    val franchisesSnap = categorySnap.child("franchises")

                    for (franchiseSnap in franchisesSnap.children) {
                        val franchiseNom = franchiseSnap.child("nom").getValue(String::class.java) ?: ""
                        val isFranchiseMatch = franchiseNom.contains(firebaseName, ignoreCase = true)

                        // cas 1 : la franchise contient des sous-sagas
                        if (franchiseSnap.hasChild("sous_sagas")) {
                            for (ssSnap in franchiseSnap.child("sous_sagas").children) {
                                val ssNom = ssSnap.child("nom").getValue(String::class.java) ?: ""
                                val isSsMatch = ssNom.contains(firebaseName, ignoreCase = true)

                                // si ça correspond à notre recherche, on récupère les films
                                if (isAllCategories || isCatMatch || isFranchiseMatch || isSsMatch) {
                                    val films = mutableListOf<FilmFirebase>()

                                    for (filmSnap in ssSnap.child("films").children) {
                                        val film = FilmFirebase(
                                            titre = filmSnap.child("titre").getValue(String::class.java) ?: "",
                                            annee = filmSnap.child("annee").getValue(Int::class.java) ?: 0,
                                            genre = filmSnap.child("genre").getValue(String::class.java) ?: universeName,
                                            numero = filmSnap.child("numero").getValue(Int::class.java) ?: 0,
                                            duree = filmSnap.child("duree").getValue(String::class.java) ?: "Inconnue",
                                            realisateur = filmSnap.child("realisateur").getValue(String::class.java) ?: "Inconnu",
                                            franchise = franchiseNom // On sauvegarde le nom de la franchise parente
                                        )
                                        films.add(film)
                                    }

                                    if (films.isNotEmpty()) {
                                        val rowName = if (isAllCategories && franchiseNom.isNotBlank() && franchiseNom != ssNom) {
                                            "$franchiseNom - $ssNom"
                                        } else ssNom

                                        result.add(SousSaga(nom = rowName, films = films.sortedBy { it.numero }))
                                    }
                                }
                            }
                        }

                        // cas 2: la franchise a ses films directement sans sous sagas (ex: Avatar, Toy Story)
                        if (franchiseSnap.hasChild("films")) {
                            if (isAllCategories || isCatMatch || isFranchiseMatch) {
                                val films = mutableListOf<FilmFirebase>()

                                for (filmSnap in franchiseSnap.child("films").children) {
                                    val film = FilmFirebase(
                                        titre = filmSnap.child("titre").getValue(String::class.java) ?: "",
                                        annee = filmSnap.child("annee").getValue(Int::class.java) ?: 0,
                                        genre = filmSnap.child("genre").getValue(String::class.java) ?: universeName,
                                        numero = filmSnap.child("numero").getValue(Int::class.java) ?: 0,
                                        duree = filmSnap.child("duree").getValue(String::class.java) ?: "Inconnue",
                                        realisateur = filmSnap.child("realisateur").getValue(String::class.java) ?: "Inconnu",
                                        franchise = franchiseNom
                                    )
                                    films.add(film)
                                }

                                if (films.isNotEmpty()) {
                                    result.add(SousSaga(nom = franchiseNom, films = films.sortedBy { it.numero }))
                                }
                            }
                        }
                    }
                }

                //regroupement pour éviter les doublons et on affiche
                val groupedResult = result.groupBy { it.nom }.map { (nom, sagas) ->
                    SousSaga(nom = nom, films = sagas.flatMap { it.films }.distinctBy { it.titre }.sortedBy { it.numero })
                }

                sousSagas = groupedResult
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) { isLoading = false }
        })
    }

    //interface graphique
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(displayTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundDark, titleContentColor = Color.White)
            )
        },
        containerColor = backgroundDark
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {//chargement en cours
                CircularProgressIndicator(color = Color(0xFFF299B5), modifier = Modifier.align(Alignment.Center))
            } else if (sousSagas.isEmpty()) {//aucun film trouvé pour l'univers en question
                Text("Aucun film trouvé pour cet univers", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            } else {//affichage liste
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {

                    //une section par sous-saga avec un carrousel horizontal de films
                    items(sousSagas) { sousSaga ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = sousSaga.nom,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)
                            )
                            //carrousel horizontal avec affiches
                            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(sousSaga.films) { film ->
                                    FilmPosterCard(film = film, navController = navController) // L'Affiche du film
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

//carte cliquable affichant l'affiche d'un film récupérée avec tmdb
@Composable
fun FilmPosterCard(film: FilmFirebase, navController: NavController) {
    var posterUrl by remember { mutableStateOf<String?>(null) }
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"

    // on va chercher l'affiche tmdb grâce au titre venant de firebase
    LaunchedEffect(film.titre) {
        try {
            val response = TmdbClient.apiService.searchMovie(myApiKey, film.titre)
            if (response.results.isNotEmpty() && response.results[0].poster_path != null) {
                posterUrl = "https://image.tmdb.org/t/p/w500${response.results[0].poster_path}"
            }
        } catch (e: Exception) { println("Erreur de chargement de l'image pour ${film.titre}") }
    }

    Box(
        modifier = Modifier
            .width(130.dp)
            .height(195.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF31343E))
            .clickable {
                // on fait l'encodage des paramètres pour éviter les problèmes avec les espaces et caractères spéciaux dans l'url
                val safeTitre = Uri.encode(if (film.titre.isNotBlank()) film.titre else "Inconnu")
                val safeGenre = Uri.encode(if (film.genre.isNotBlank()) film.genre else "Inconnu")
                val safeDuree = Uri.encode(if (film.duree.isNotBlank()) film.duree else "Inconnue")
                val safeRealisateur = Uri.encode(if (film.realisateur.isNotBlank()) film.realisateur else "Inconnu")
                val safeFranchise = Uri.encode(if (film.franchise.isNotBlank()) film.franchise else "Inconnue")

                navController.navigate("movie/$safeTitre/${film.annee}/$safeGenre/$safeDuree/$safeRealisateur/$safeFranchise")
            },
        contentAlignment = Alignment.Center
    ) {
        if (posterUrl != null) { //affiche tmdb disponible
            AsyncImage(model = posterUrl, contentDescription = film.titre, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            // si tmdb n'a pas trouvé
            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(film.titre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${film.annee}", color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}