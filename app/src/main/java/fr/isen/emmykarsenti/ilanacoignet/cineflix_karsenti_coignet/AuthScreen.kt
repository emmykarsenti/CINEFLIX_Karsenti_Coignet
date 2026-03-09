package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController // <-- L'import de Navigation est ici
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // ON FORCE UN FOND BLANC ET DES TEXTES NOIRS POUR LE TEST
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Fond blanc forcé
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CINEFLIX",
            fontSize = 36.sp,
            color = Color.Black, // Texte noir forcé
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Ajoute "color = TextFieldDefaults.colors(...)" ou force juste le style
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Adresse E-mail") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // BOUTON CONNEXION
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    // Appel à Firebase pour connecter l'utilisateur
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Connexion réussie !", Toast.LENGTH_SHORT).show()
                                // L'UTILISATEUR EST CONNECTÉ, ON L'ENVOIE À L'ACCUEIL :
                                navController.navigate("home")
                            } else {
                                Toast.makeText(context, "Erreur : ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Se connecter")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // BOUTON INSCRIPTION
        TextButton(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    // Appel à Firebase pour CRÉER un utilisateur
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Compte créé avec succès !", Toast.LENGTH_SHORT).show()
                                // LE COMPTE EST CRÉÉ, ON L'ENVOIE À L'ACCUEIL :
                                navController.navigate("home")
                            } else {
                                Toast.makeText(context, "Erreur : ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text("Créer un compte")
        }
    }
}