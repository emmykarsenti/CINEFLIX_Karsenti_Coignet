package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet.ui.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


// 1. MODÈLES DE DONNÉES (Pour lire l'API)

// L'API nous renvoie un objet qui contient une liste "results"
data class MovieResponse(
    val results: List<TmdbMovie>
)

// Les détails d'un seul film
data class TmdbMovie(
    val id: Int,
    val title: String,
    val poster_path: String?,    // Affiche verticale (jaquette classique)
    val backdrop_path: String?,  // Affiche horizontale (parfaite pour le carrousel du haut)
    val release_date: String?    // Date de sortie
)

// 2. INTERFACE API (Les requêtes qu'on peut faire)
interface TmdbApiService {

    // Requête magique pour filtrer, trier et récupérer exactement ce qu'on veut
    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("with_companies") companyId: String, // Permet de filtrer par studio (ex: Disney = 2)
        @Query("language") language: String = "fr-FR", // Pour avoir les titres et résumés en français
        @Query("sort_by") sortBy: String = "popularity.desc", // Le tri par défaut (les plus populaires d'abord)
        @Query("primary_release_date.lte") maxDate: String? = null // Permet d'exclure les films qui ne sont pas encore sortis (lte = Less Than or Equal)
    ): MovieResponse

    // Requête pour la barre de recherche
    @GET("search/movie")
    suspend fun searchMovie(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "fr-FR"
    ): MovieResponse
}

// 3. CLIENT RETROFIT (Le moteur de connexion)
object TmdbClient {
    private const val BASE_URL = "https://api.themoviedb.org/3/"

    val apiService: TmdbApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApiService::class.java)
    }
}

object PosterCache {
    val posters = mutableMapOf<String, String>()
    val years = mutableMapOf<String, String>()
    val genres = mutableMapOf<String, String>()
}