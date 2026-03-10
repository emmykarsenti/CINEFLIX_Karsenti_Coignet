package fr.isen.emmykarsenti.ilanacoignet.cineflix_karsenti_coignet

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// 1. Les modèles de données pour lire la réponse de l'API
data class MovieResponse(
    val results: List<TmdbMovie>
)

data class TmdbMovie(
    val id: Int,
    val title: String,
    val poster_path: String?,
    val release_date: String?
)

// 2. L'interface qui définit les requêtes possibles
interface TmdbApiService {
    // On cherche les films, avec la possibilité de filtrer par studio (Disney, Marvel, etc.)
    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("with_companies") companyId: String,
        @Query("language") language: String = "fr-FR",
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): MovieResponse

    @GET("search/movie")
    suspend fun searchMovie(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "fr-FR"
    ): MovieResponse

}

// 3. L'objet Retrofit prêt à l'emploi
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