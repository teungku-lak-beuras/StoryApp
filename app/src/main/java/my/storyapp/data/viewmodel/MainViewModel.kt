package my.storyapp.data.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import my.storyapp.data.MainRepository
import my.storyapp.data.retrofit.ListStoryItem
import my.storyapp.data.retrofit.LoginResult

class MainViewModel(private val repository: MainRepository) : ViewModel() {
    private var storiesLoaded = false
    private var mutableCurrentImageUri: MutableLiveData<Uri?> = MutableLiveData()
    private var pagingStories: LiveData<PagingData<ListStoryItem>>? = null
    private var updateAdapter = false
    private var latestFirstStory = ListStoryItem("", "", "", "", "", 0.0, 0.0)

    val currentImageUri: LiveData<Uri?> = mutableCurrentImageUri

    fun login(email: String, password: String) = repository.login(email, password)

    fun register(name: String, email: String, password: String) = repository.register(name, email, password)

    fun getUser() = repository.getUser()

    fun setUser(loginResult: LoginResult) = repository.setUser(loginResult)

    fun deleteUser() = repository.deleteUser()

    fun getPagingStories(): LiveData<PagingData<ListStoryItem>> {
        if (pagingStories == null) {
            pagingStories = repository.getPagingStories().cachedIn(viewModelScope)
        }

        return pagingStories as LiveData<PagingData<ListStoryItem>>
    }

    fun postStory(
        context: Context,
        description: String,
        imageUri: Uri,
        latitude: Double? = null,
        longitude: Double? = null
    ) = repository.postStory(
        context,
        description,
        imageUri,
        latitude,
        longitude
    )

    fun setStoriesLoaded(isStoriesLoaded: Boolean) {
        storiesLoaded = isStoriesLoaded
    }

    fun setCurrentImageUri(currentImageUri: Uri) {
        this.mutableCurrentImageUri.value = currentImageUri
    }

    fun shallUpdateAdapter(): Boolean {
        return updateAdapter
    }

    fun setShallUpdateAdapter(value: Boolean) {
        updateAdapter = value
    }

    fun getLatestFirstStory(): ListStoryItem {
        return latestFirstStory
    }

    fun setLatestFirstStory(story: ListStoryItem) {
        latestFirstStory = story
    }
}
