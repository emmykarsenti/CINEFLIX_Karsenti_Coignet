package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen

import android.content.Context
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.R

// écran d'authentification de l'application (login / register) qui gère la connexion/création de compte/la mémorisation de session
@Composable
fun AuthScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance() // instance de firebase authentication
    val sharedPreferences = context.getSharedPreferences("CineflixPrefs", Context.MODE_PRIVATE) // fichier de sauvegarde local pour mémoriser l'état "rester connecté" entre les sessions

    //champs de saisie
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") } // seulement l'inscription
    var rememberMe by remember { mutableStateOf(false) }
    var isInscription by remember { mutableStateOf(false) } // bascule entre le mode connexion (false) et inscription (true)

    //vérification automatique de session au lancement de l'écran
    LaunchedEffect(Unit) {
        val isRememberMeChecked = sharedPreferences.getBoolean("remember_me", false)
        if (auth.currentUser != null) { //si un user est déjà connecté dans firebase
            if (isRememberMeChecked) {
                //le user avait coché "rester connecté" donc on le redirige directement vers l'accueil
                //popUpTo empêche de revenir sur cet écran avec le bouton retour
                navController.navigate("home") { popUpTo("auth") { inclusive = true } }
            } else {
                //si pas de case cochée on déconnecte pour forcer la saisie des identifiants
                auth.signOut()
            }
        }
    }

    //interface user
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) // Fond noir complet pour l'écran de login
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, // Centrage horizontal de tous les éléments
        verticalArrangement = Arrangement.Center // Centrage vertical global
    ) {
        //logo cineflix
        Image(
            painter = painterResource(id = R.drawable.logo_cineflix_noir),
            contentDescription = "Logo Cineflix",
            modifier = Modifier
                .height(330.dp)
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            contentScale = ContentScale.Fit
        )

        //champ mail
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Adresse E-mail", color = Color(0xFFF299B5)) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFF299B5), unfocusedBorderColor = Color(0xFFF299B5),
                focusedLabelColor = Color(0xFFF299B5), unfocusedLabelColor = Color(0xFFF299B5),
                cursorColor = Color(0xFFF299B5)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        //champ mot de passe
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe", color = Color(0xFFF299B5)) },
            visualTransformation = PasswordVisualTransformation(), // masque les caractères saisis
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFF299B5), unfocusedBorderColor = Color(0xFFF299B5),
                focusedLabelColor = Color(0xFFF299B5), unfocusedLabelColor = Color(0xFFF299B5),
                cursorColor = Color(0xFFF299B5)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        //champ username uniquement si inscription
        if (isInscription) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nom d'utilisateur", color = Color(0xFFF299B5)) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFF299B5), unfocusedBorderColor = Color(0xFFF299B5),
                    focusedLabelColor = Color(0xFFF299B5), unfocusedLabelColor = Color(0xFFF299B5),
                    cursorColor = Color(0xFFF299B5)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // case à cocher "rester connecté"
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFF299B5),
                    checkmarkColor = Color.Black,
                    uncheckedColor = Color.White
                )
            )
            Text(text = "Rester connecté", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // gestion des boutons selon le mode choisi
        if (!isInscription) { //mode connexion
            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) { // vérifier si champs non vide
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // sauvegarde du choix "rester connecté" dans les SharedPreferences
                                    sharedPreferences.edit().putBoolean("remember_me", rememberMe).apply()
                                    Toast.makeText(context, "Connexion réussie !", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home") { popUpTo("auth") { inclusive = true } }
                                } else {
                                    // affichage erreur firebase (ex: mot de passe incorrect, compte inexistant)
                                    Toast.makeText(context, "Erreur : ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF299B5))
            ) {
                Text("Se connecter", fontSize = 14.sp, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lien pour passer en mode inscription
            TextButton(onClick = { isInscription = true }) {
                Text("Créer un compte", color = Color.LightGray)
            }

        } else { //mode inscription
            Button(
                onClick = {
                    //vérifie que l'email, le mot de passe et le pseudo sont remplis
                    if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener // récupération de l'uid généré par firebase pour ce nouvel utilisateur

                                    //sauvegarde du username dans firebase sous : users/{uid}/username
                                    FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
                                        .getReference("users/$uid/username")
                                        .setValue(username)

                                    // sauvegarde du choix "rester connecté"
                                    sharedPreferences.edit().putBoolean("remember_me", rememberMe).apply()
                                    Toast.makeText(context, "Compte créé avec succès !", Toast.LENGTH_SHORT).show()

                                    //redirection vers la page d'accueil
                                    navController.navigate("home") { popUpTo("auth") { inclusive = true } }
                                } else {
                                    // affichage de l'erreur firebase (ex: email déjà utilisé, mot de passe trop faible)
                                    Toast.makeText(context, "Erreur : ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF299B5))
            ) {
                Text("Créer mon compte", fontSize = 14.sp, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // lien pour revenir au mode connexion
            TextButton(onClick = { isInscription = false }) {
                Text("Déjà un compte ? Se connecter", color = Color.LightGray)
            }
        }
    }
}