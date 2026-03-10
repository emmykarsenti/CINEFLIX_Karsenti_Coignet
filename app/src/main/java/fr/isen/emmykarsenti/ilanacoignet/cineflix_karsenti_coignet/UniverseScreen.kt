/* package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
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
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniverseScreen(navController: NavController, universeName: String) {
    val backgroundDark = Color(0xFF1A1D29)

    // On filtre les films selon l'univers cliqué
    val movies = MockData.myMovies.filter { it.universe == universeName }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(universeName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
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

        if (movies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Aucun film disponible", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                items(movies) { movie ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { },  // on branchera le détail après
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = movie.posterUrl,
                            contentDescription = movie.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(2f / 3f)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = movie.title,
                            color = Color.White,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}*/

package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

// Modèle d'un film Firebase
data class FilmFirebase(
    val titre: String = "",
    val annee: Int = 0,
    val genre: String = "",
    val numero: Int = 0
)

// Modèle d'une sous-saga
data class SousSaga(
    val nom: String = "",
    val films: List<FilmFirebase> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniverseScreen(navController: NavController, universeName: String) {
    val backgroundDark = Color(0xFF1A1D29)

    // Mapping entre le nom affiché et le nom dans Firebase
    val firebaseName = when (universeName) {
        "Marvel" -> "Marvel Cinematic Universe"
        "Star Wars" -> "Star Wars"
        "Avatar" -> "Avatar"
        "Indiana Jones" -> "Indiana Jones"
        "Pixar" -> "Pixar" // à adapter selon ton JSON
        "Disney" -> "Disney" // à adapter selon ton JSON
        else -> universeName
    }

    var sousSagas by remember { mutableStateOf<List<SousSaga>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Charger les films depuis Firebase
    LaunchedEffect(universeName) {
        val db = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app").reference

        db.child("categories").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result = mutableListOf<SousSaga>()

                // On parcourt toutes les catégories
                for (categorySnap in snapshot.children) {
                    // On parcourt toutes les franchises de cette catégorie
                    val franchisesSnap = categorySnap.child("franchises")
                    for (franchiseSnap in franchisesSnap.children) {
                        val nom = franchiseSnap.child("nom").getValue(String::class.java) ?: ""

                        // On trouve la bonne franchise
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
                            Icons.Default.ArrowBack,
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
    ) /*{ padding ->

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE50914))
                }
            }
            sousSagas.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Aucun film trouvé", color = Color.Gray)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    sousSagas.forEach { sousSaga ->
                        item {
                            // Titre de la sous-saga
                            Text(
                                text = sousSaga.nom,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(sousSaga.films) { film ->
                            FilmRow(film = film)
                        }
                    }
                }
            }
        }
    }
}*/
    { padding ->
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
                            FilmRow(film = film)
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun FilmRow(film: FilmFirebase) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF31343E))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Numéro du film
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