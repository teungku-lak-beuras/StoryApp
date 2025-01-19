package my.storyapp.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import my.storyapp.data.retrofit.ListStoryItem

@Database(entities = [ListStoryItem::class, RemoteKeyEntity::class], version = 2, exportSchema = false)
abstract class StoryDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun remoteKeysDao(): RemoteKeyDao

    companion object {
        @Volatile
        private var INSTANCE: StoryDatabase? = null

        fun getInstance(context: Context): StoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, StoryDatabase::class.java, "stories.db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
