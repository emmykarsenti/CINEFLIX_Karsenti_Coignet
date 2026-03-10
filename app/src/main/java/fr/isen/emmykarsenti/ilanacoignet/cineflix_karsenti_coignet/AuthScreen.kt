package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_cineflix_noir),
            contentDescription = "Logo Cineflix",
            modifier = Modifier
                .height(330.dp)
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            contentScale = ContentScale.Fit
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Adresse E-mail", color = Color(0xFFF299B5)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFF299B5),
                unfocusedBorderColor = Color(0xFFF299B5),
                focusedLabelColor = Color(0xFFF299B5),
                unfocusedLabelColor = Color(0xFFF299B5),
                cursorColor = Color(0xFFF299B5)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe", color = Color(0xFFF299B5)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFF299B5),
                unfocusedBorderColor = Color(0xFFF299B5),
                focusedLabelColor = Color(0xFFF299B5),
                unfocusedLabelColor = Color(0xFFF299B5),
                cursorColor = Color(0xFFF299B5)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Connexion réussie !", Toast.LENGTH_SHORT).show()
                                navController.navigate("home")
                            } else {
                                Toast.makeText(context, "Erreur : ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B2FA3))
        ) {
            Text("Se connecter")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Compte créé avec succès !", Toast.LENGTH_SHORT).show()
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
            Text("Créer un compte", color = Color.LightGray)
        }
    }
}