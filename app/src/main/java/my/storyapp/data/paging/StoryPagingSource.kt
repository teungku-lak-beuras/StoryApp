package my.storyapp.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import my.storyapp.data.MainRepository
import my.storyapp.data.retrofit.ListStoryItem

class StoryPagingSource(private val repository: MainRepository) : PagingSource<Int, ListStoryItem>() {
    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let {anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)

            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val token = "Bearer ${repository.getUser().token}"
            val response = repository.apiService.getPagingStories(token, position, params.loadSize)

            return LoadResult.Page(
                data = response.listStory,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (response.listStory.isEmpty()) null else position + 1
            )
        }
        catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}
