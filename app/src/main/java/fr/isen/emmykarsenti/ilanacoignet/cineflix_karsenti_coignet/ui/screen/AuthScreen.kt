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

/**
 * Écran d'authentification de l'application (Login / Register).
 * Gère la connexion, la création de compte, et la mémorisation de session.
 */
@Composable
fun AuthScreen(navController: NavController) {
    // Récupération du contexte Android (nécessaire pour les Toasts et les SharedPreferences)
    val context = LocalContext.current

    // Instance de Firebase Authentication
    val auth = FirebaseAuth.getInstance()

    // SharedPreferences : petit fichier de sauvegarde local pour mémoriser l'état "Rester connecté"
    val sharedPreferences = context.getSharedPreferences("CineflixPrefs", Context.MODE_PRIVATE)

    // ÉTATS DE L'INTERFACE
    // Variables stockant la saisie de l'utilisateur en temps réel
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") } // Utilisé uniquement à l'inscription

    // État de la case à cocher "Rester connecté"
    var rememberMe by remember { mutableStateOf(false) }

    // Toggle pour basculer entre l'interface de Connexion (false) et d'Inscription (true)
    var isInscription by remember { mutableStateOf(false) }

    /**
     * Vérification automatique de la session au démarrage de l'écran.
     * S'exécute une seule fois (grâce à Unit).
     */
    LaunchedEffect(Unit) {
        val isRememberMeChecked = sharedPreferences.getBoolean("remember_me", false)

        // Si un utilisateur est déjà connecté dans Firebase
        if (auth.currentUser != null) {
            if (isRememberMeChecked) {
                // S'il avait coché "Rester connecté", on le redirige directement vers l'accueil.
                // popUpTo("auth") { inclusive = true } détruit l'écran de login pour empêcher d'y revenir avec le bouton "Retour".
                navController.navigate("home") { popUpTo("auth") { inclusive = true } }
            } else {
                // S'il n'avait pas coché la case, on le déconnecte de force par sécurité.
                auth.signOut()
            }
        }
    }

    // CONSTRUCTION DE L'INTERFACE UTILISATEUR
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) // Fond noir complet pour l'écran de login
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, // Centrage horizontal de tous les éléments
        verticalArrangement = Arrangement.Center // Centrage vertical global
    ) {
        // Logo de l'application
        Image(
            painter = painterResource(id = R.drawable.logo_cineflix_noir),
            contentDescription = "Logo Cineflix",
            modifier = Modifier
                .height(330.dp)
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            contentScale = ContentScale.Fit
        )

        // Champ de saisie : Adresse E-mail
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

        // Champ de saisie : Mot de passe
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe", color = Color(0xFFF299B5)) },
            visualTransformation = PasswordVisualTransformation(), // Masque les caractères saisis (points noirs)
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFF299B5), unfocusedBorderColor = Color(0xFFF299B5),
                focusedLabelColor = Color(0xFFF299B5), unfocusedLabelColor = Color(0xFFF299B5),
                cursorColor = Color(0xFFF299B5)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Champ de saisie : Nom d'utilisateur (Visible UNIQUEMENT si l'utilisateur veut créer un compte)
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

        // Case à cocher "Rester connecté"
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFF299B5), // Couleur quand cochée
                    checkmarkColor = Color.Black,     // Couleur de la coche
                    uncheckedColor = Color.White      // Couleur de la bordure quand vide
                )
            )
            Text(text = "Rester connecté", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GESTION DES BOUTONS SELON LE MODE (CONNEXION ou INSCRIPTION)
        if (!isInscription) { // MODE CONNEXION

            // Bouton de validation de connexion
            Button(
                onClick = {
                    // Vérifie que les champs ne sont pas vides
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        // Appel à Firebase pour se connecter
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Sauvegarde du choix "Rester connecté" dans les SharedPreferences
                                    sharedPreferences.edit().putBoolean("remember_me", rememberMe).apply()
                                    Toast.makeText(context, "Connexion réussie !", Toast.LENGTH_SHORT).show()

                                    // Navigation vers l'accueil en détruisant l'historique de l'écran de connexion
                                    navController.navigate("home") { popUpTo("auth") { inclusive = true } }
                                } else {
                                    // Affichage de l'erreur Firebase (ex: mot de passe incorrect, compte inexistant)
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

            // Lien texte pour basculer vers le mode Inscription
            TextButton(onClick = { isInscription = true }) {
                Text("Créer un compte", color = Color.LightGray)
            }

        } else { // MODE INSCRIPTION

            // Bouton de validation d'inscription
            Button(
                onClick = {
                    // Vérifie que l'email, le mot de passe ET le pseudo sont remplis
                    if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()) {
                        // Appel à Firebase pour créer un compte
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Récupération de l'ID unique (UID) généré par Firebase pour ce nouvel utilisateur
                                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                                    // Sauvegarde du pseudo choisi dans la base de données Realtime Database
                                    // Le chemin sera : users/{UID}/username = "LePseudo"
                                    FirebaseDatabase.getInstance("https://cineflix-karsenti-coignet-default-rtdb.europe-west1.firebasedatabase.app")
                                        .getReference("users/$uid/username")
                                        .setValue(username)

                                    // Sauvegarde du choix "Rester connecté"
                                    sharedPreferences.edit().putBoolean("remember_me", rememberMe).apply()
                                    Toast.makeText(context, "Compte créé avec succès !", Toast.LENGTH_SHORT).show()

                                    // Redirection immédiate vers l'accueil
                                    navController.navigate("home") { popUpTo("auth") { inclusive = true } }
                                } else {
                                    // Affichage de l'erreur Firebase (ex: email déjà utilisé, mot de passe trop faible)
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

            // Lien texte pour revenir au mode Connexion
            TextButton(onClick = { isInscription = false }) {
                Text("Déjà un compte ? Se connecter", color = Color.LightGray)
            }
        }
    }
}