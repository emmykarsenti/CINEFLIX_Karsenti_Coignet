package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.data

// modèle pour un film
data class Film(
    val id: String = "",
    val title: String = "", // ex: skywalker
    val universe: String = "", // ex: marvel, disney, star wars...
    val releaseDate: String = "",
    val category: String = "" // ex:skywalker saga
)


// modèle pour les actions du user sur un film (vu, à voir, possède...)
data class UserFilmAction(
    val userId: String = "", // id user
    val filmId: String = "", // id film
    val isWatched: Boolean = false, // vu
    val wantToWatch: Boolean = false, // à voir
    val ownOnDVD: Boolean = false, // possède sur dvd
    val wantToGetRidOf: Boolean = false //veut s'en séparer
)
