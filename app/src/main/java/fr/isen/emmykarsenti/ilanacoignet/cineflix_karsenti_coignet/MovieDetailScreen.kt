package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import androidx.compose.foundation.layout.*
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
    var currentStatus by remember { mutableStateOf<String?>(null) }

    // Charger le statut actuel depuis Firebase
    LaunchedEffect(titre) {
        val uid = currentUser?.uid ?: return@LaunchedEffect
        FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies/$uid/$titre")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentStatus = snapshot.getValue(String::class.java)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun setStatus(status: String) {
        val uid = currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("userMovies/$uid/$titre")
        if (currentStatus == status) {
            ref.removeValue()
        } else {
            ref.setValue(status)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titre, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1D29),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1A1D29)
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Infos du film
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

            // Statuts
            if (currentUser != null) {
                Text(
                    "Mon statut",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatusBtn("✅ Vu", "WATCHED", currentStatus) { setStatus("WATCHED") }
                    StatusBtn("🎯 À voir", "WANT_TO_WATCH", currentStatus) { setStatus("WANT_TO_WATCH") }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatusBtn("📀 Je possède", "OWN_DVD", currentStatus) { setStatus("OWN_DVD") }
                    StatusBtn("💸 Vendre", "WANT_TO_SELL", currentStatus) { setStatus("WANT_TO_SELL") }
                }
            } else {
                Text(
                    "Connectez-vous pour gérer votre collection",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun RowScope.StatusBtn(
    label: String,
    status: String,
    currentStatus: String?,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (currentStatus == status) Color(0xFFE50914) else Color(0xFF2C2C54)
        )
    ) {
        Text(label, fontSize = 11.sp, color = Color.White)
    }
}