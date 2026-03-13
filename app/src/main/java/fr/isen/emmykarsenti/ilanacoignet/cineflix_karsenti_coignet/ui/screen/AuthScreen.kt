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
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.R

@Composable
fun AuthScreen(navController: NavController) {
    // 1. INITIALISATION DES OUTILS

    // Le contexte permet d'interagir avec le système Android (ex: afficher des Toasts ou lire la mémoire)
    val context = LocalContext.current

    // Instance de Firebase pour gérer l'authentification
    val auth = FirebaseAuth.getInstance()

    // SharedPreferences : C'est la mémoire interne (le "coffre-fort") du téléphone.
    // On crée un fichier nommé "CineflixPrefs" en mode privé (seule notre app peut le lire).
    val sharedPreferences = context.getSharedPreferences("CineflixPrefs", Context.MODE_PRIVATE)

    // 2. VARIABLES D'ÉTAT (Gérées par Compose)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // Cette variable gère l'état visuel et logique de la case à cocher (décochée par défaut)
    var rememberMe by remember { mutableStateOf(false) }

    // 3. VÉRIFICATION AUTOMATIQUE AU LANCEMENT
    // LaunchedEffect(Unit) s'exécute une seule fois au moment où cet écran apparaît à l'image.
    LaunchedEffect(Unit) {
        // On va lire dans la mémoire si l'utilisateur avait coché la case la dernière fois.
        // Si on ne trouve rien, la valeur par défaut sera 'false'.
        val isRememberMeChecked = sharedPreferences.getBoolean("remember_me", false)

        // Si Firebase a gardé une session utilisateur active en mémoire...
        if (auth.currentUser != null) {
            if (isRememberMeChecked) {
                // ...ET que l'utilisateur voulait qu'on se souvienne de lui :
                // On le téléporte directement sur l'écran d'accueil sans qu'il ne voie rien !
                navController.navigate("home") {
                    // popUpTo nettoie l'historique : ça empêche de revenir sur la page de login
                    // en appuyant sur le bouton "Retour" du téléphone.
                    popUpTo("auth") { inclusive = true }
                }
            } else {
                // ...MAIS qu'il n'avait pas coché la case :
                // On le déconnecte de force pour l'obliger à retaper son mot de passe.
                auth.signOut()
            }
        }
    }

    // 4. INTERFACE UTILISATEUR (UI)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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

        // Champ pour l'adresse e-mail
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

        // Champ pour le mot de passe (avec masquage des caractères)
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

        // 5. CASE À COCHER "RESTER CONNECTÉ"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = rememberMe,
                // Quand on clique dessus, on met à jour la variable d'état
                onCheckedChange = { rememberMe = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFF299B5), // Couleur de fond quand c'est coché
                    checkmarkColor = Color.Black,     // Couleur du "V" à l'intérieur
                    uncheckedColor = Color.White      // Couleur des bordures quand c'est décoché
                )
            )
            Text(text = "Rester connecté", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. BOUTON "SE CONNECTER"
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // CONNEXION RÉUSSIE !
                                // On ouvre notre mémoire SharedPreferences pour y sauvegarder l'état actuel
                                // de la case à cocher (true ou false) avec la clé "remember_me".
                                sharedPreferences.edit().putBoolean("remember_me", rememberMe).apply()

                                Toast.makeText(context, "Connexion réussie !", Toast.LENGTH_SHORT).show()

                                // On navigue vers l'accueil en détruisant cet écran de connexion de l'historique
                                navController.navigate("home") {
                                    popUpTo("auth") { inclusive = true }
                                }
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

        // 7. BOUTON "CRÉER UN COMPTE"
        TextButton(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // CRÉATION RÉUSSIE !
                                // On fait exactement la même chose : on sauvegarde le choix de l'utilisateur
                                // pour qu'il n'ait pas à se reconnecter s'il a coché la case pendant l'inscription.
                                sharedPreferences.edit().putBoolean("remember_me", rememberMe).apply()

                                Toast.makeText(context, "Compte créé avec succès !", Toast.LENGTH_SHORT).show()

                                // Navigation vers l'accueil sans retour possible sur le login
                                navController.navigate("home") {
                                    popUpTo("auth") { inclusive = true }
                                }
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