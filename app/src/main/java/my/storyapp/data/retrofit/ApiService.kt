package my.storyapp.data.retrofit

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

// @FormUrlEncoded untuk mengirim data pengguna menggunakan x-www-form-urlencoded.

interface ApiService {
    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @GET("stories")
    fun getLocatedStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("location") location: Int = 1
    ): Call<StoryResponse>

    @GET("stories")
    suspend fun getPagingStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): StoryResponse

    @Multipart
    @POST("stories")
    fun postStory(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<RegisterResponse>

    @Multipart
    @POST("stories")
    fun postStoryWithLocation(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part,
        @Part("lat") lat: Double,
        @Part("lon") lon: Double
    ): Call<RegisterResponse>
}
