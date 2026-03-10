package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var watchedMovies by remember { mutableStateOf<List<String>>(emptyList()) }
    var wantToWatchMovies by remember { mutableStateOf<List<String>>(emptyList()) }
    var ownedMovies by remember { mutableStateOf<List<String>>(emptyList()) }
    var wantToSellMovies by remember { mutableStateOf<List<String>>(emptyList()) }

    fun removeMovieFromFirebase(movieTitle: String) {
        val uid = currentUser?.uid ?: return
        FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies/$uid/$movieTitle")
            .removeValue() // Supprime l'objet entier du film (statut de visionnage + possession)
    }

    LaunchedEffect(currentUser) {
        val uid = currentUser?.uid
        if (uid != null) {
            val dbRef = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("userMovies/$uid")

            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempWatched = mutableListOf<String>()
                    val tempWantToWatch = mutableListOf<String>()
                    val tempOwned = mutableListOf<String>()
                    val tempWantToSell = mutableListOf<String>()

                    for (movieSnap in snapshot.children) {
                        val title = movieSnap.key ?: continue

                        // NOUVEAU : On lit les deux propriétés du film
                        val wStatus = movieSnap.child("watch_status").getValue(String::class.java)
                        val oStatus = movieSnap.child("own_status").getValue(String::class.java)

                        if (wStatus == "WATCHED") tempWatched.add(title)
                        if (wStatus == "WANT_TO_WATCH") tempWantToWatch.add(title)

                        if (oStatus == "OWN_DVD") tempOwned.add(title)
                        if (oStatus == "WANT_TO_SELL") tempWantToSell.add(title)
                    }

                    watchedMovies = tempWatched
                    wantToWatchMovies = tempWantToWatch
                    ownedMovies = tempOwned
                    wantToSellMovies = tempWantToSell
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Mon Profil", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Connecté en tant que :", fontSize = 16.sp)
        Text(text = currentUser?.email ?: "Utilisateur inconnu", fontSize = 20.sp, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (watchedMovies.isEmpty() && wantToWatchMovies.isEmpty() && ownedMovies.isEmpty() && wantToSellMovies.isEmpty()) {
                item {
                    Text(text = "Vous n'avez encore ajouté aucun film à votre collection.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (ownedMovies.isNotEmpty()) {
                item { CategoryTitle("📀 Mes films possédés :") }
                items(ownedMovies) { movieTitle ->
                    MovieCard(
                        title = "📀 $movieTitle",
                        onClick = { navController.navigate("movie/$movieTitle/-/-") },
                        onDeleteClick = { removeMovieFromFirebase(movieTitle) }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            if (watchedMovies.isNotEmpty()) {
                item { CategoryTitle("✅ Films vus :") }
                items(watchedMovies) { movieTitle ->
                    MovieCard(
                        title = "✅ $movieTitle",
                        onClick = { navController.navigate("movie/$movieTitle/-/-") },
                        onDeleteClick = { removeMovieFromFirebase(movieTitle) }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            if (wantToWatchMovies.isNotEmpty()) {
                item { CategoryTitle("🎯 À voir :") }
                items(wantToWatchMovies) { movieTitle ->
                    MovieCard(
                        title = "🎯 $movieTitle",
                        onClick = { navController.navigate("movie/$movieTitle/-/-") },
                        onDeleteClick = { removeMovieFromFirebase(movieTitle) }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            if (wantToSellMovies.isNotEmpty()) {
                item { CategoryTitle("💸 À vendre :") }
                items(wantToSellMovies) { movieTitle ->
                    MovieCard(
                        title = "💸 $movieTitle",
                        onClick = { navController.navigate("movie/$movieTitle/-/-") },
                        onDeleteClick = { removeMovieFromFirebase(movieTitle) }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                auth.signOut()
                navController.navigate("auth") { popUpTo(0) }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Se déconnecter")
        }
    }
}

@Composable
fun CategoryTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

// NOUVEAU : Ajout du onClick sur la Card entière !
@Composable
fun MovieCard(title: String, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }, // Rend la carte cliquable
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Supprimer de la liste",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}