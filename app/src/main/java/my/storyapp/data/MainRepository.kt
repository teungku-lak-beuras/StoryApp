@file:OptIn(ExperimentalPagingApi::class)

package my.storyapp.data

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import my.storyapp.data.paging.StoryPagingMediator
import my.storyapp.data.retrofit.ApiService
import my.storyapp.data.retrofit.ListStoryItem
import my.storyapp.data.retrofit.LoginResponse
import my.storyapp.data.retrofit.LoginResult
import my.storyapp.data.retrofit.RegisterResponse
import my.storyapp.data.retrofit.StoryResponse
import my.storyapp.data.room.StoryDatabase
import my.storyapp.data.sharedpreferences.UserPreferences
import my.storyapp.data.sharedpreferences.UserSessionEntity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainRepository private constructor(
    val apiService: ApiService,
    val database: StoryDatabase,
    val userPreferences: UserPreferences,
    val dataExecutor: DataExecutor
) {
    fun login(email: String, password: String): LiveData<LoginResponse> {
        val response = apiService.login(email, password)
        val result = MutableLiveData<LoginResponse>()

        dataExecutor.networkIO.execute {
            response.enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    // CASE: 100% Success
                    if (response.isSuccessful) {
                        result.value = response.body()
                    }
                    // CASE: API defined failure like invalid password, invalid etc..
                    else {
                        val errorBody = response.errorBody()?.string().toString()
                        val message = JSONObject(errorBody).getString("message")
                        val errorStatus = JSONObject(errorBody).getBoolean("error")
                        val failureResponse = LoginResponse(errorStatus, message)

                        result.value = failureResponse
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, response: Throwable) {
                    val failureResponse = LoginResponse(true, response.message.toString())
                    result.value = failureResponse
                }
            })
        }

        return result
    }

    fun register(name: String, email: String, password: String): LiveData<RegisterResponse> {
        val response = apiService.register(name, email, password)
        val result = MutableLiveData<RegisterResponse>()

        dataExecutor.networkIO.execute {
            response.enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    // CASE: 100% Success
                    if (response.isSuccessful) {
                        result.value = response.body()
                    }
                    // CASE: API defined failure like invalid password, invalid etc..
                    else {
                        val errorBody = response.errorBody()?.string().toString()
                        val message = JSONObject(errorBody).getString("message")
                        val errorStatus = JSONObject(errorBody).getBoolean("error")
                        val failureResponse = RegisterResponse(errorStatus, message)

                        result.value = failureResponse
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, response: Throwable) {
                    val failureResponse = RegisterResponse(true, response.message.toString())
                    result.value = failureResponse
                }
            })
        }

        return result
    }

    fun getUser() = userPreferences.getUser()

    fun setUser(loginResult: LoginResult) = userPreferences.setUser(UserSessionEntity(loginResult.userId, loginResult.name, loginResult.token))

    fun deleteUser() = userPreferences.deleteUser()

    fun getLocatedStories(): MutableLiveData<StoryResponse> {
        val token = "Bearer ${getUser().token}"
        val result = MutableLiveData<StoryResponse>()

        val response = apiService.getLocatedStories(token = token)

        response.enqueue(object : Callback<StoryResponse> {
            override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>) {
                if (response.isSuccessful) {
                    result.value = response.body()
                }
                else {
                    val errorBody = response.errorBody()?.string().toString()
                    val message = JSONObject(errorBody).getString("message")
                    val errorStatus = JSONObject(errorBody).getBoolean("error")
                    val failureResponse = StoryResponse(errorStatus, message)

                    result.value = failureResponse
                }
            }

            override fun onFailure(call: Call<StoryResponse>, response: Throwable) {
                val failureResponse = StoryResponse(true, response.message.toString())
                result.value = failureResponse
            }
        })

        return result
    }

    fun getPagingStories(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = {
                database.storyDao().getStoriesFromDatabase()
            },
            remoteMediator = StoryPagingMediator(this)
        ).liveData
    }

    fun postStory(context: Context, description: String, imageUri: Uri, latitude: Double? = null, longitude: Double? = null): LiveData<RegisterResponse> {
        val postDescription = description.toRequestBody("text/plain".toMediaType())
        val mutableResult = MutableLiveData<RegisterResponse>()
        val token = "Bearer ${getUser().token}"
        val result: LiveData<RegisterResponse> = mutableResult

        dataExecutor.networkIO.execute {
            val file = provideFile(context, imageUri)
            val postPhoto = MultipartBody.Part.createFormData("photo", file.name, file.asRequestBody("image/jpeg".toMediaType()))
            lateinit var response: Call<RegisterResponse>

            if (latitude != null && longitude != null) {
                response = apiService.postStoryWithLocation(token, postDescription, postPhoto, latitude, longitude)
            }
            else {
                response = apiService.postStory(token, postDescription, postPhoto)
            }

            response.enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    // CASE: 100% Success
                    if (response.isSuccessful) {
                        mutableResult.value = response.body()!!
                    }
                    // CASE: API defined failure like invalid password, invalid etc..
                    else {
                        val errorBody = response.errorBody()?.string().toString()
                        val message = JSONObject(errorBody).getString("message")
                        val errorStatus = JSONObject(errorBody).getBoolean("error")
                        val failureResponse = RegisterResponse(errorStatus, message)

                        mutableResult.value = failureResponse
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, response: Throwable) {
                    val failureResponse = RegisterResponse(true, response.message.toString())
                    mutableResult.value = failureResponse
                }
            })
        }

        return result
    }

    companion object {
        @Volatile
        private var INSTANCE: MainRepository? = null

        fun getInstance(apiService: ApiService, database: StoryDatabase, userPreferences: UserPreferences, dataExecutor: DataExecutor): MainRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = MainRepository(apiService, database, userPreferences, dataExecutor)
                INSTANCE = instance
                instance
            }
        }
    }
}
