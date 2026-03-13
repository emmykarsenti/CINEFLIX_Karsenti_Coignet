/* package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

data class FilmEnVente(
    val titre: String,
    val vendeurPseudo: String,
    val posterUrl: String?
)

@Composable
fun MarketScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val coroutineScope = rememberCoroutineScope()
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"

    var filmsEnVente by remember { mutableStateOf<List<FilmEnVente>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

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
        FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val result = mutableListOf<FilmEnVente>()

                    for (userSnap in snapshot.children) {
                        val uid = userSnap.key ?: continue
                        if (uid == currentUser?.uid) continue
                        val pseudo = "Utilisateur_${uid.take(5)}"

                        for (filmSnap in userSnap.children) {
                            val titre = filmSnap.key ?: continue
                            val ownStatus = filmSnap.child("own_status").getValue(String::class.java)
                            if (ownStatus == "WANT_TO_SELL") {
                                result.add(FilmEnVente(titre, pseudo, null))
                            }
                        }
                    }

                    coroutineScope.launch {
                        filmsEnVente = result.map { film ->
                            film.copy(posterUrl = fetchPoster(film.titre))
                        }
                        isLoading = false
                    }
                }
                override fun onCancelled(error: DatabaseError) { isLoading = false }
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1D29))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Achat/Revente", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF299B5))
        Text("Films proposés par la communauté CinéFlix", fontSize = 14.sp, color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFF299B5))
            }
        } else if (filmsEnVente.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun film en vente pour le moment.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filmsEnVente) { film ->
                    Card(
                        onClick = { navController.navigate("movie/${film.titre}/-/-") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF31343E))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                    fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("💸 En vente", color = Color(0xFFF299B5), fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("👤 ${film.vendeurPseudo}", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
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

data class FilmEnVente(
    val titre: String,
    val vendeurPseudo: String,
    val posterUrl: String?
)

@Composable
fun MarketScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val coroutineScope = rememberCoroutineScope()
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"
    val prixDefaut = "5€"

    var filmsEnVente by remember { mutableStateOf<List<FilmEnVente>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

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
/*
    LaunchedEffect(Unit) {
        FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val result = mutableListOf<FilmEnVente>()

                    for (userSnap in snapshot.children) {
                        val uid = userSnap.key ?: continue
                        if (uid == currentUser?.uid) continue
                        val pseudo = "Utilisateur_${uid.take(5)}"

                        for (filmSnap in userSnap.children) {
                            val titre = filmSnap.key ?: continue
                            val ownStatus = filmSnap.child("own_status").getValue(String::class.java)
                            if (ownStatus == "WANT_TO_SELL") {
                                result.add(FilmEnVente(titre, pseudo, null))
                            }
                        }
                    }

                    coroutineScope.launch {
                        filmsEnVente = result.map { it.copy(posterUrl = fetchPoster(it.titre)) }
                        isLoading = false
                    }
                }
                override fun onCancelled(error: DatabaseError) { isLoading = false }
            })
    }*/

    // APRÈS
    LaunchedEffect(Unit) {
        val db = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")

        db.getReference("userMovies")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val result = mutableListOf<FilmEnVente>()
                    var usersRestants = snapshot.children.count { it.key != currentUser?.uid }

                    if (usersRestants == 0) {
                        isLoading = false
                        return
                    }

                    for (userSnap in snapshot.children) {
                        val uid = userSnap.key ?: continue
                        if (uid == currentUser?.uid) continue

                        db.getReference("users/$uid/username")
                            .get()
                            .addOnSuccessListener { usernameSnap ->
                                val pseudo = usernameSnap.getValue(String::class.java)
                                    ?: "Utilisateur_${uid.take(5)}"

                                for (filmSnap in userSnap.children) {
                                    val titre = filmSnap.key ?: continue
                                    val ownStatus = filmSnap.child("own_status").getValue(String::class.java)
                                    if (ownStatus == "WANT_TO_SELL") {
                                        result.add(FilmEnVente(titre, pseudo, null))
                                    }
                                }

                                usersRestants--
                                if (usersRestants == 0) {
                                    coroutineScope.launch {
                                        filmsEnVente = result.map { it.copy(posterUrl = fetchPoster(it.titre)) }
                                        isLoading = false
                                    }
                                }
                            }
                    }
                }
                override fun onCancelled(error: DatabaseError) { isLoading = false }
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1D29))
            .padding(16.dp) ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Achat/Revente", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF299B5))
        Text("Films proposés par la communauté CinéFlix", fontSize = 14.sp, color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFF299B5))
            }
        } else if (filmsEnVente.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun film en vente pour le moment.", color = Color.Gray)
            }
        } else {
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
                                Text("👤 ${film.vendeurPseudo}", color = Color.Gray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(" $prixDefaut", color = Color(0xFFF299B5), fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
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
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}