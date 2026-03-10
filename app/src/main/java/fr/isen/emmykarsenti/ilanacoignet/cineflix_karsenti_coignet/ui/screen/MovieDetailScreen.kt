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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    navController: NavController,
    titre: String,
    annee: String,
    genre: String
) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    // 1. Deux variables séparées pour les deux types de statuts
    var watchStatus by remember { mutableStateOf<String?>(null) }
    var ownStatus by remember { mutableStateOf<String?>(null) }

    // 2. Variable pour stocker la liste des vendeurs
    var sellers by remember { mutableStateOf<List<String>>(emptyList()) }

    // 3. Charger les statuts actuels de l'utilisateur connecté
    LaunchedEffect(titre) {
        val uid = currentUser?.uid ?: return@LaunchedEffect
        FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies/$uid/$titre")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // On lit les deux statuts indépendamment
                    watchStatus = snapshot.child("watch_status").getValue(String::class.java)
                    ownStatus = snapshot.child("own_status").getValue(String::class.java)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // 4. Chercher tous les utilisateurs qui veulent vendre CE film
    LaunchedEffect(titre) {
        val dbRef = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sellersList = mutableListOf<String>()

                for (userSnap in snapshot.children) {
                    val userUid = userSnap.key ?: continue

                    // On vérifie le statut de possession ("own_status") pour ce film
                    val status = userSnap.child(titre).child("own_status").getValue(String::class.java)

                    // Si l'utilisateur vend le film ET que ce n'est pas nous-même
                    if (status == "WANT_TO_SELL" && userUid != currentUser?.uid) {
                        // On crée un pseudo fictif avec le début de son UID (car on n'a pas son email ici)
                        val pseudo = "Utilisateur_" + userUid.take(5)
                        sellersList.add(pseudo)
                    }
                }
                sellers = sellersList
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // 5. Fonctions pour mettre à jour les statuts dans Firebase
    fun setWatchStatus(status: String) {
        val uid = currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies/$uid/$titre/watch_status")

        // Si on clique sur le statut déjà actif, ça l'annule (le supprime)
        if (watchStatus == status) ref.removeValue() else ref.setValue(status)
    }

    fun setOwnStatus(status: String) {
        val uid = currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies/$uid/$titre/own_status")

        if (ownStatus == status) ref.removeValue() else ref.setValue(status)
    }

    Scaffold(
        topBar = {
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
        containerColor = Color(0xFF1A1D29)
    ) { padding ->

        // Utilisation d'un LazyColumn pour pouvoir faire défiler la page s'il y a beaucoup de vendeurs
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Infos du film
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF31343E))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(titre, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("📅 Année : $annee", color = Color.LightGray, fontSize = 14.sp)
                        Text("🎬 Genre : $genre", color = Color.LightGray, fontSize = 14.sp)
                    }
                }
            }

            // Boutons d'action de l'utilisateur
            item {
                if (currentUser != null) {
                    Text("Mon statut de visionnage", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        StatusBtn("✅ Vu", "WATCHED", watchStatus, Modifier.weight(1f)) { setWatchStatus("WATCHED") }
                        StatusBtn("🎯 À voir", "WANT_TO_WATCH", watchStatus, Modifier.weight(1f)) { setWatchStatus("WANT_TO_WATCH") }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("Ma collection", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        StatusBtn("📀 Je possède", "OWN_DVD", ownStatus, Modifier.weight(1f)) { setOwnStatus("OWN_DVD") }
                        StatusBtn("💸 Vendre", "WANT_TO_SELL", ownStatus, Modifier.weight(1f)) { setOwnStatus("WANT_TO_SELL") }
                    }
                } else {
                    Text("Connectez-vous pour gérer votre collection", color = Color.Gray, fontSize = 14.sp)
                }
            }

            // Section "Aspect Social" : Affichage des vendeurs
            item {
                Spacer(Modifier.height(16.dp))
                Text("🛒 Membres cédant ce film", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                if (sellers.isEmpty()) {
                    Text("Aucun utilisateur ne souhaite se séparer de ce film pour le moment.", color = Color.Gray, fontSize = 14.sp)
                }
            }

            // Liste dynamique des vendeurs
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
        }
    }
}

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
            containerColor = if (currentStatus == status) Color(0xFFE50914) else Color(0xFF2C2C54)
        )
    ) {
        Text(label, fontSize = 12.sp, color = Color.White)
    }
}