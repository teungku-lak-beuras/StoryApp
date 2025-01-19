package my.storyapp.data

import android.content.Context
import my.storyapp.data.retrofit.ApiConfig
import my.storyapp.data.room.StoryDatabase
import my.storyapp.data.sharedpreferences.UserPreferences

class Injector {
    companion object {
        fun injectAndProvideRepository(context: Context): MainRepository {
            val database = StoryDatabase.getInstance(context)
            val userPreferences = UserPreferences(context)
            val dataExecutor = DataExecutor()
            val token = userPreferences.getUser().token

            if (token.isNotEmpty()) {
                val apiService = ApiConfig.getApiService()
                return MainRepository.getInstance(apiService, database, userPreferences, dataExecutor)
            }

            val apiService = ApiConfig.getApiService()

            return MainRepository.getInstance(apiService, database, userPreferences, dataExecutor)
        }
    }
}
