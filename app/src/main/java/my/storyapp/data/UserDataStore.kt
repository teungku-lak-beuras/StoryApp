package my.storyapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserDataStore private constructor(private val dataStore: DataStore<Preferences>) {
    companion object {
        @Volatile
        private var INSTANCE: UserDataStore? = null

        fun getInstance(dataStore: DataStore<Preferences>): UserDataStore {
            return INSTANCE ?: synchronized(this) {
                val instance = UserDataStore(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}
