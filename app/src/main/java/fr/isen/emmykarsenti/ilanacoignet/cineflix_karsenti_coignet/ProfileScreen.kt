package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Mon Profil", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))

        // Affichage de l'email de l'utilisateur connecté
        Text(text = "Connecté en tant que :", fontSize = 16.sp)
        Text(text = currentUser?.email ?: "Utilisateur inconnu", fontSize = 20.sp, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Mes films possédés :", fontSize = 22.sp, modifier = Modifier.align(Alignment.Start))
        // TODO: Afficher ici la liste des films possédés depuis Firebase

        Spacer(modifier = Modifier.weight(1f)) // Pousse le bouton vers le bas

        // Bouton de déconnexion
        Button(
            onClick = {
                auth.signOut()
                // On retourne à la page de connexion et on vide l'historique de navigation
                navController.navigate("auth") {
                    popUpTo(0)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Se déconnecter")
        }
    }
}