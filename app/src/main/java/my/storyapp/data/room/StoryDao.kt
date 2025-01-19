package my.storyapp.data.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import my.storyapp.data.retrofit.ListStoryItem

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: List<ListStoryItem>)

    @Query("select * from stories")
    fun getStoriesFromDatabase(): PagingSource<Int, ListStoryItem>

    @Query("delete from stories")
    suspend fun deleteStoriesFromDatabase()
}
