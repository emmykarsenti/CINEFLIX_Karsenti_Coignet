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

data class FilmFirebase(
    val titre: String = "",
    val annee: Int = 0,
    val genre: String = "",
    val numero: Int = 0
)

data class SousSaga(
    val nom: String = "",
    val films: List<FilmFirebase> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniverseScreen(navController: NavController, universeName: String) {
    val backgroundDark = Color(0xFF1A1D29)

    val firebaseName = when (universeName) {
        "Marvel" -> "Marvel Cinematic Universe"
        "Star Wars" -> "Star Wars"
        "Avatar" -> "Avatar"
        "Indiana Jones" -> "Indiana Jones"
        "Pixar" -> "Pixar"
        "Disney" -> "Disney"
        else -> universeName
    }

    var sousSagas by remember { mutableStateOf<List<SousSaga>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Ta logique Firebase intacte !
    LaunchedEffect(universeName) {
        val db =
            FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app").reference

        db.child("categories").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result = mutableListOf<SousSaga>()

                for (categorySnap in snapshot.children) {
                    val franchisesSnap = categorySnap.child("franchises")
                    for (franchiseSnap in franchisesSnap.children) {
                        val nom = franchiseSnap.child("nom").getValue(String::class.java) ?: ""

                        if (nom == firebaseName) {
                            val sousSagasSnap = franchiseSnap.child("sous_sagas")
                            for (ssSnap in sousSagasSnap.children) {
                                val ssNom = ssSnap.child("nom").getValue(String::class.java) ?: ""
                                val films = mutableListOf<FilmFirebase>()

                                for (filmSnap in ssSnap.child("films").children) {
                                    val film = FilmFirebase(
                                        titre = filmSnap.child("titre").getValue(String::class.java)
                                            ?: "",
                                        annee = filmSnap.child("annee").getValue(Int::class.java)
                                            ?: 0,
                                        genre = filmSnap.child("genre").getValue(String::class.java)
                                            ?: "",
                                        numero = filmSnap.child("numero").getValue(Int::class.java)
                                            ?: 0
                                    )
                                    films.add(film)
                                }
                                result.add(
                                    SousSaga(
                                        nom = ssNom,
                                        films = films.sortedBy { it.numero })
                                )
                            }
                        }
                    }
                }
                sousSagas = result
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
            }
        })
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(universeName, fontWeight = FontWeight.Bold) },
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
    ) { innerPadding -> // <--- ON A RENOMMÉ ICI
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) { // <--- ET ICI
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFFE50914),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (sousSagas.isEmpty()) {
                Text(
                    "Aucun film trouvé",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Affichage style Disney+ : Colonne de Sagas, et Ligne de films pour chaque saga
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(sousSagas) { sousSaga ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Titre de la sous-saga (ex: "Phase 1")
                            // Titre de la sous-saga (ex: "Phase 1")
                            Text(
                                text = sousSaga.nom,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp) // <--- CORRECTION ICI
                            )

                            // Carrousel horizontal des films de cette saga
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(sousSaga.films) { film ->
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

// Le nouveau composant qui cherche l'affiche et l'affiche au format Disney+
@Composable
fun FilmPosterCard(film: FilmFirebase, navController: NavController) {
    var posterUrl by remember { mutableStateOf<String?>(null) }
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"

    // Recherche de l'affiche sur TMDB en utilisant le titre Firebase
    LaunchedEffect(film.titre) {
        try {
            val response = TmdbClient.apiService.searchMovie(myApiKey, film.titre)
            if (response.results.isNotEmpty() && response.results[0].poster_path != null) {
                posterUrl = "https://image.tmdb.org/t/p/w500${response.results[0].poster_path}"
            }
        } catch (e: Exception) {
            println("Erreur de chargement de l'image pour ${film.titre}")
        }
    }

    Box(
        modifier = Modifier
            .width(130.dp)
            .height(195.dp) // Ratio affiche classique
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF31343E)) // Fond gris si l'image charge ou n'existe pas
            .clickable {
                navController.navigate("movie/${film.titre}/${film.annee}/${film.genre}")
            },
        contentAlignment = Alignment.Center
    ) {
        if (posterUrl != null) {
            AsyncImage(
                model = posterUrl,
                contentDescription = film.titre,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Affichage de secours (Fallback) si TMDB ne trouve pas l'affiche
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
