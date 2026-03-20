package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

//réponse de l'api : un objet contenant une liste de films
data class MovieResponse(
    val results: List<TmdbMovie>
)

// Les détails d'un seul film retourvé par tmdn api
data class TmdbMovie(
    val id: Int,                // identifiant unique
    val title: String,        // titre principal
    val poster_path: String?,    // affiche verticale (jaquette classique)
    val backdrop_path: String?,  // affiche horizontale (parfaite pour le carrousel du haut)
    val release_date: String?,    // aate de sortie
    val overview: String?,        // résumé
    val genre_ids: List<Int>? = null
)

//on définit les requêtes disponibles vers l'api tmdb
interface TmdbApiService {

    //recherche avancée : permet de filtrer par studio, genre, date, popularité...
    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String, // clé api tmdb
        @Query("with_companies") companyId: String? = null, // permet de filtrer par univers (ex: 2|3|420|1|574)
        @Query("language") language: String = "fr-FR", // pour avoir les titres et résumés en français
        @Query("sort_by") sortBy: String = "popularity.desc", // tri par défaut (les plus populaires d'abord)
        @Query("primary_release_date.lte") maxDate: String? = null, // permet d'exclure les films qui ne sont pas encore sortis (lte = less than or equal)
        @Query("with_genres") withGenres: String? = null // filtrer par genre (ex: 35 = comedie)
    ): MovieResponse

    //recherche par titre : utilisée pour la barre de recherche et la récupération d'affiches
    @GET("search/movie")
    suspend fun searchMovie(
        @Query("api_key") apiKey: String, //clé api tmdb
        @Query("query") query: String, //texte saisie par le user
        @Query("language") language: String = "fr-FR" //avoir les titres et résumés en français
    ): MovieResponse
}

//client retrofit configuré pour communiquer avec l'api tmdb
object TmdbClient {
    private const val BASE_URL = "https://api.themoviedb.org/3/" //url de base de l'api tmdb


    val apiService: TmdbApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApiService::class.java)
    }
}

//clé = titre du film, valeur = donnée associée
object PosterCache {
    val posters = mutableMapOf<String, String>()
    val years = mutableMapOf<String, String>()
    val genres = mutableMapOf<String, String>()
}