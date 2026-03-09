package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet


object MockData {

    val myMovies = listOf(
        Movie(
            id = "1",
            title = "Iron Man",
            universe = "Marvel",
            releaseDate = "30/04/2008",
            category = "Phase 1"
        ),
        Movie(
            id = "2",
            title = "Star Wars : Un nouvel espoir",
            universe = "Star Wars",
            releaseDate = "19/10/1977",
            category = "Skywalker Saga"
        ),
        Movie(
            id = "3",
            title = "Toy Story",
            universe = "Pixar",
            releaseDate = "27/03/1996",
            category = null
        ),
        Movie(
            id = "4",
            title = "Avatar",
            universe = "Avatar",
            releaseDate = "16/12/2009",
            category = null
        )
    )
}