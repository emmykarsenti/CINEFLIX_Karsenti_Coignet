package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.data

data class Movie(
    val id: String = "",
    val title: String = "",
    val universe: String = "",
    val releaseDate: String = "",
    val category: String? = null,
    val posterUrl: String = ""
)