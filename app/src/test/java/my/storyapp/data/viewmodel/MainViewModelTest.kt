package my.storyapp.data.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import my.storyapp.DataDummy
import my.storyapp.MainDispatcherRule
import my.storyapp.data.MainRepository
import my.storyapp.data.adapter.StoryPagingAdapter
import my.storyapp.data.retrofit.ListStoryItem
import my.storyapp.getOrAwaitValue
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

/*
 * Ketika berhasil memuat data.
 *
 * Memastikan data tidak null.
 * Memastikan jumlah data sesuai dengan yang diharapkan.
 * Memastikan data pertama yang dikembalikan sesuai.
 *
 * Ketika tidak ada data.
 *
 * Memastikan jumlah data yang dikembalikan nol.
 */

class StoryPagingSource : PagingSource<Int, LiveData<List<ListStoryItem>>>() {
    companion object {
        fun snapshot(items: List<ListStoryItem>): PagingData<ListStoryItem> {
            return PagingData.from(items)
        }
    }
    override fun getRefreshKey(state: PagingState<Int, LiveData<List<ListStoryItem>>>): Int {
        return 0
    }
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<ListStoryItem>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }
}

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()
    @Mock
    private lateinit var repository: MainRepository

    val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }

    @Test
    fun `Data tidak null jumlahnya sesuai dan data yang pertama itu sama`() = runTest {
        val dummyStories = DataDummy.generateDummyStoryResponse()
        val data: PagingData<ListStoryItem> = StoryPagingSource.snapshot(dummyStories)
        val expectedStory = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStory.value = data

        Mockito.`when`(repository.getPagingStories()).thenReturn(expectedStory)

        val viewmodel = MainViewModel(repository)
        val actualStory: PagingData<ListStoryItem> = viewmodel.getPagingStories().getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryPagingAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )

        differ.submitData(actualStory)
        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStories.size, differ.snapshot().size)
        Assert.assertEquals(dummyStories[0], differ.snapshot()[0])
    }

    @Test
    fun `Kalau ceritanya kosong maka harus mengembalikan data kosong juga`() = runTest {
        val data: PagingData<ListStoryItem> = PagingData.from(emptyList())
        val expectedStory = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStory.value = data

        Mockito.`when`(repository.getPagingStories()).thenReturn(expectedStory)

        val viewmodel = MainViewModel(repository)
        val actualStory: PagingData<ListStoryItem> = viewmodel.getPagingStories().getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryPagingAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )

        differ.submitData(actualStory)
        Assert.assertEquals(0, differ.snapshot().size)
    }
}
