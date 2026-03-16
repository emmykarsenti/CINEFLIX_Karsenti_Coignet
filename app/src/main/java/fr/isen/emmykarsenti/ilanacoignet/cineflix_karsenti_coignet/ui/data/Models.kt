package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.data

// Modèle pour un Film
data class Film(
    val id: String = "",
    val title: String = "", // ex: Skywalker
    val universe: String = "", // ex: Marvel, Disney, Star Wars...
    val releaseDate: String = "",
    val category: String = "" // ex: Skywalker Saga
)


// Modèle pour les actions de l'utilisateur sur un film (Vu, À voir, Possède...)
data class UserFilmAction(
    val userId: String = "", // Identifiant de l'utilisateur
    val filmId: String = "", // Identifiant du film
    val isWatched: Boolean = false, // Vu
    val wantToWatch: Boolean = false, // À voir
    val ownOnDVD: Boolean = false, // Possède sur DVD
    val wantToGetRidOf: Boolean = false // Ne veut plus voir
)
