package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.data.TmdbClient
import fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.data.TmdbMovie
import kotlinx.coroutines.launch

/**
 * Écran affichant tous les films correspondants à un genre spécifique (Action, Comédie, etc.).
 * L'affichage se fait sous forme de grille (style Netflix/Galerie).
 * @param navController Utilisé pour la navigation entre les écrans.
 * @param genreName Le nom du genre à afficher en titre (ex: "Action").
 * @param genreId L'identifiant TMDB du genre pour effectuer la requête API.
 */
@OptIn(ExperimentalMaterial3Api::class) // Nécessaire pour utiliser TopAppBar qui est encore expérimentale dans Material 3
@Composable
fun GenreScreen(navController: NavController, genreName: String, genreId: String) {
    // Clé API TMDB pour autoriser les requêtes
    val myApiKey = "9b06bfc70be38627cb51e3cb6d008512"

    // ÉTATS DE L'INTERFACE
    // movies : stocke la liste des films récupérés depuis l'API. Met à jour l'UI quand la liste change.
    var movies by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }
    // isLoading : permet d'afficher un indicateur de chargement (roue qui tourne) pendant la requête API.
    var isLoading by remember { mutableStateOf(true) }

    // Scope pour lancer la requête API de manière asynchrone sans bloquer l'interface
    val coroutineScope = rememberCoroutineScope()

    /**
     * LaunchedEffect s'exécute au lancement du composant ou lorsque 'genreId' change.
     * C'est ici que l'on effectue l'appel réseau pour récupérer les films.
     */
    LaunchedEffect(genreId) {
        coroutineScope.launch {
            try {
                // IDs des studios franchisés ciblés par l'application (Disney, Pixar, Marvel, StarWars, Avatar)
                val myUniverses = "2|3|420|1|574"

                // Appel au endpoint "discover" de l'API TMDB.
                // Contrairement à la recherche textuelle, ce endpoint permet de filtrer rigoureusement
                // par "companyId", garantissant que seuls les films de nos franchises s'affichent ici.
                val response = TmdbClient.apiService.discoverMovies(
                    apiKey = myApiKey,
                    companyId = myUniverses, // <-- Force l'API à n'utiliser QUE ces studios
                    withGenres = genreId,    // <-- Applique le filtre de genre cliqué
                    sortBy = "popularity.desc" // Trie les résultats par popularité décroissante
                )

                // On filtre les résultats pour ne garder que les films qui possèdent une affiche (poster_path != null)
                movies = response.results.filter { it.poster_path != null }
            } catch (e: Exception) {
                // Si la requête échoue (pas d'internet, erreur serveur...), on loggue l'erreur
                e.printStackTrace()
            } finally {
                // Quoi qu'il arrive (succès ou échec de la requête), on arrête l'animation de chargement
                isLoading = false
            }
        }
    }

    // Scaffold fournit une structure de base Material Design avec des emplacements standard (TopBar, Content, BottomBar...)
    Scaffold(
        topBar = {
            // Barre d'application en haut de l'écran
            TopAppBar(
                title = { Text("Films : $genreName", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    // Bouton retour pour revenir à l'écran précédent
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1D29), // Couleur de fond sombre correspondant au thème
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1A1D29) // Fond global de la page
    ) { padding ->
        // Le contenu de la page doit respecter le padding fourni par le Scaffold (pour ne pas passer sous la TopBar)
        if (isLoading) {
            // Affichage de l'indicateur de chargement rouge centré à l'écran
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE50914))
            }
        } else {
            // Affichage des films sous forme de grille une fois le chargement terminé
            LazyVerticalGrid(
                columns = GridCells.Fixed(3), // Fixe le nombre de colonnes à 3
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 16.dp), // Espace en haut et en bas de la grille
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Espace horizontal entre les colonnes
                verticalArrangement = Arrangement.spacedBy(16.dp) // Espace vertical entre les lignes
            ) {
                // Parcourt la liste des films récupérés
                items(movies) { movie ->
                    // Affiche l'image du film de manière asynchrone via la bibliothèque Coil
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w500${movie.poster_path}", // URL complète de l'affiche
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop, // Recadre l'image pour qu'elle remplisse tout l'espace alloué sans se déformer
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f) // Impose un ratio standard de poster de film (ex: 600x900)
                            .clip(RoundedCornerShape(8.dp)) // Arrondit les coins de l'affiche
                            .background(Color(0xFF31343E)) // Fond visible si l'image met du temps à charger
                            .clickable {
                                // Lors du clic sur une affiche, on navigue vers l'écran de détail du film.
                                // On récupère l'année de sortie (les 4 premiers caractères de la date)
                                val annee = movie.release_date?.take(4) ?: "Inconnue"

                                // Uri.encode sécurise les variables pour l'URL de navigation (remplace les espaces par %20, gère les /, etc.)
                                val safeTitre = Uri.encode(movie.title.ifBlank { "Inconnu" })

                                // Navigation vers la route MovieDetailScreen avec tous ses paramètres requis
                                navController.navigate("movie/$safeTitre/$annee/${Uri.encode(genreName)}/Inconnue/Inconnu/Pop Culture")
                            }
                    )
                }
            }
        }
    }
}