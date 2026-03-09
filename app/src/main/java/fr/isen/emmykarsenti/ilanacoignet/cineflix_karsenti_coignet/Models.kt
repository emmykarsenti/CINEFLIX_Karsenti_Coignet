package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

// Modèle pour un Film
data class Film(
    val id: String = "",
    val title: String = "",
    val universe: String = "", // ex: Marvel, Disney, Star Wars...
    val releaseDate: String = "",
    val category: String = "" // ex: Skywalker Saga
)

// Modèle pour les actions de l'utilisateur sur un film (Vu, À voir, Possède...)
data class UserFilmAction(
    val userId: String = "",
    val filmId: String = "",
    val isWatched: Boolean = false,
    val wantToWatch: Boolean = false,
    val ownOnDVD: Boolean = false,
    val wantToGetRidOf: Boolean = false
)