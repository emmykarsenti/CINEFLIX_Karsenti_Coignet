package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

// 1. Le modèle pour un Film
data class Film(
    val titre: String = "",
    val annee: Int = 0,
    val genre: String = "",
    val numero: Int = 0
)

// 2. Le modèle pour une Franchise (ex: Star Wars, Marvel)
data class Franchise(
    val nom: String = "",
    val films: List<Film>? = null,
    val sous_sagas: List<SousSaga>? = null // Certaines ont des sous-sagas
)

// 3. Le modèle pour les Sous-Sagas (spécifique à Star Wars par ex)
data class SousSaga(
    val nom: String = "",
    val films: List<Film> = emptyList()
)

// 4. Le modèle pour une Catégorie (le plus haut niveau de ton JSON)
data class Category(
    val categorie: String = "",
    val franchises: List<Franchise> = emptyList()
)