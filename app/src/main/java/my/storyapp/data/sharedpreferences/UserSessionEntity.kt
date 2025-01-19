package my.storyapp.data.sharedpreferences

data class UserSessionEntity(
    val userId: String,
    val name: String,
    val token: String,
)
