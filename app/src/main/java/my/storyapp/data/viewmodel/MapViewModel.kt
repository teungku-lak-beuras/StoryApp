package my.storyapp.data.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import my.storyapp.data.MainRepository
import my.storyapp.data.retrofit.StoryResponse

class MapViewModel(private val repository: MainRepository) : ViewModel() {
    private var mutableLocatedStories = repository.getLocatedStories()
    var locatedStories: LiveData<StoryResponse> = mutableLocatedStories

    fun getLocatedStories(lifecycleOwner: LifecycleOwner) {
        val liveData = repository.getLocatedStories()
        val observer = Observer<StoryResponse> { value ->
            mutableLocatedStories.value = value
        }

        liveData.observe(lifecycleOwner, observer)
    }
}
