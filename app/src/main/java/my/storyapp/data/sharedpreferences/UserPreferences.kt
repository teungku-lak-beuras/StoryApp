package my.storyapp.data.sharedpreferences

import android.content.Context

class UserPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFERENCES_NAME = "user_preferences"
        private const val USER_ID = "user_id"
        private const val NAME = "name"
        private const val TOKEN = "token"
    }

    fun getUser(): UserSessionEntity {
        val userId = preferences.getString(USER_ID, "").toString()
        val name = preferences.getString(NAME, "").toString()
        val token = preferences.getString(TOKEN, "").toString()

        return UserSessionEntity(userId, name, token)
    }

    fun setUser(userSessionEntity: UserSessionEntity) {
        val editor = preferences.edit()

        editor.putString(USER_ID, userSessionEntity.userId)
        editor.putString(NAME, userSessionEntity.name)
        editor.putString(TOKEN, userSessionEntity.token)
        editor.apply()
    }

    fun deleteUser() {
        val editor = preferences.edit()

        editor.remove(USER_ID)
        editor.remove(NAME)
        editor.remove(TOKEN)
        editor.apply()
    }
}
