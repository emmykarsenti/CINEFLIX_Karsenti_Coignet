package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.*

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

    LaunchedEffect(universeName) {
        val db = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app").reference

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
                                        titre = filmSnap.child("titre").getValue(String::class.java) ?: "",
                                        annee = filmSnap.child("annee").getValue(Int::class.java) ?: 0,
                                        genre = filmSnap.child("genre").getValue(String::class.java) ?: "",
                                        numero = filmSnap.child("numero").getValue(Int::class.java) ?: 0
                                    )
                                    films.add(film)
                                }
                                result.add(SousSaga(nom = ssNom, films = films.sortedBy { it.numero }))
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
                    containerColor = Color(0xFF1A1D29),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = backgroundDark
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFFE50914),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (sousSagas.isEmpty()) {
                Text(
                    text = "Aucun film trouvé",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    sousSagas.forEach { sousSaga ->
                        item {
                            Text(
                                text = sousSaga.nom,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(sousSaga.films) { film ->
                            FilmRow(film = film, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilmRow(film: FilmFirebase, navController: NavController) {
    Card(
        onClick = { navController.navigate("movie/${film.titre}/${film.annee}/${film.genre}") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF31343E))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${film.numero}",
                color = Color(0xFFE50914),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.width(30.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(film.titre, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text("${film.annee} · ${film.genre}", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}