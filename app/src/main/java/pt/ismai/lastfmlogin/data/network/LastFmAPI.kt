package pt.ismai.lastfmlogin.data.network


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pt.ismai.lastfmlogin.data.model.LoginResponse
import pt.ismai.lastfmlogin.data.model.RecentTracksResponse
import pt.ismai.lastfmlogin.data.model.UserInfoResponse
import pt.ismai.lastfmlogin.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LastFmApi {
    @FormUrlEncoded
    @POST(".")
    suspend fun getMobileSession(@FieldMap params: Map<String, String>): LoginResponse

    @GET(".")
    suspend fun getUserInfo(
        @Query("method") method: String = "user.getInfo",
        @Query("user") username: String,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json"
    ): UserInfoResponse

    @GET(".")
    suspend fun getRecentTracks(
        @Query("method") method: String = "user.getrecenttracks",
        @Query("user") username: String,
        @Query("api_key") apiKey: String,
        @Query("limit") limit: Int = 20,
        @Query("format") format: String = "json"
    ): RecentTracksResponse
}

object RetrofitClient {
    val api: LastFmApi by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        val headerInterceptor = okhttp3.Interceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("User-Agent", "SpotFM/1.0.0")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LastFmApi::class.java)
    }
}