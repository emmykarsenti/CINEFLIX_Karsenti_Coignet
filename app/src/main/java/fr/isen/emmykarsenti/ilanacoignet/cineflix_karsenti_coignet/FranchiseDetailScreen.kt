package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FranchiseDetailScreen(navController: NavController, catIndex: String?, franIndex: String?) {
    var franchise by remember { mutableStateOf<Franchise?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) } // On ajoute la gestion d'erreur

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // On récupère les données avec la même méthode robuste que le HomeScreen
    LaunchedEffect(catIndex, franIndex) {
        if (catIndex != null && franIndex != null) {
            val database = Firebase.database.reference
            database.child("categories").get()
                .addOnSuccessListener { snapshot ->
                    val list = mutableListOf<Category>()
                    snapshot.children.forEach { child ->
                        val cat = child.getValue(Category::class.java)
                        if (cat != null) list.add(cat)
                    }
                    try {
                        val cIdx = catIndex.toInt()
                        val fIdx = franIndex.toInt()
                        // On sélectionne la bonne franchise dans la liste locale
                        franchise = list[cIdx].franchises[fIdx]
                    } catch (e: Exception) {
                        errorMessage = "Oups, impossible de charger les films. 🎬"
                    }
                }
                .addOnFailureListener {
                    errorMessage = "Erreur de connexion à la base de données."
                }
        } else {
            errorMessage = "Informations de navigation manquantes."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(franchise?.nom ?: "Chargement...", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF05001E))
            )
        },
        containerColor = Color(0xFF05001E)
    ) { padding ->
        // On centre le contenu pour le chargement ou l'erreur
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {

            if (errorMessage != null) {
                // S'il y a une erreur, on l'affiche EN ROUGE au milieu de l'écran
                Text(text = errorMessage!!, color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            } else if (franchise == null) {
                // Chargement en cours (normalement très court)
                CircularProgressIndicator(color = Color.White)
            } else {
                // Tout va bien, on affiche la liste des films de la franchise
                LazyColumn(modifier = Modifier.fillMaxSize()) {

                    // CAS 1 : Sous-sagas (Ex: Star Wars)
                    franchise!!.sous_sagas?.let { sousSagas ->
                        items(sousSagas) { saga ->
                            Text(
                                text = saga.nom,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                            )
                            saga.films.forEach { film ->
                                FilmItem(film, currentUserId)
                            }
                        }
                    }

                    // CAS 2 : Films directs (Ex: Avatar)
                    franchise!!.films?.let { films ->
                        items(films) { film ->
                            FilmItem(film, currentUserId)
                        }
                    }
                }
            }
        }
    }
}

// Composant pour afficher un seul film ET gérer ses statuts
@Composable
fun FilmItem(film: Film, userId: String) {
    // Nettoyage du titre pour qu'il soit accepté comme clé Firebase
    val safeTitle = film.titre.replace(Regex("[.#$\\[\\]]"), "")

    val dbRef = Firebase.database.reference.child("users").child(userId).child("films").child(safeTitle)

    var isWatched by remember { mutableStateOf(false) }
    var isToWatch by remember { mutableStateOf(false) }
    var isOwned by remember { mutableStateOf(false) }
    var isToGive by remember { mutableStateOf(false) }

    LaunchedEffect(film.titre) {
        if (userId.isNotEmpty()) {
            dbRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    isWatched = snapshot.child("vu").getValue(Boolean::class.java) ?: false
                    isToWatch = snapshot.child("a_voir").getValue(Boolean::class.java) ?: false
                    isOwned = snapshot.child("possede").getValue(Boolean::class.java) ?: false
                    isToGive = snapshot.child("a_donner").getValue(Boolean::class.java) ?: false
                }
            }
        }
    }

    fun updateFirebase(vu: Boolean, aVoir: Boolean, possede: Boolean, aDonner: Boolean) {
        val data = mapOf(
            "titre" to film.titre,
            "vu" to vu,
            "a_voir" to aVoir,
            "possede" to possede,
            "a_donner" to aDonner
        )
        dbRef.setValue(data)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D29))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = film.titre, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Année : ${film.annee} | Genre : ${film.genre}",
                color = Color.LightGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusButton("Vu", isWatched) {
                    isWatched = !isWatched; updateFirebase(isWatched, isToWatch, isOwned, isToGive)
                }
                StatusButton("À voir", isToWatch) {
                    isToWatch = !isToWatch; updateFirebase(isWatched, isToWatch, isOwned, isToGive)
                }
                StatusButton("Possédé", isOwned) {
                    isOwned = !isOwned; updateFirebase(isWatched, isToWatch, isOwned, isToGive)
                }
                StatusButton("À donner", isToGive) {
                    isToGive = !isToGive; updateFirebase(isWatched, isToWatch, isOwned, isToGive)
                }
            }
        }
    }
}

@Composable
fun StatusButton(text: String, isActive: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primary else Color(0xFF33384D),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}