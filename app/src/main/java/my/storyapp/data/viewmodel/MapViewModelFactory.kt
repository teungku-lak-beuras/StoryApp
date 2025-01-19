package my.storyapp.data.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import my.storyapp.data.Injector
import my.storyapp.data.MainRepository

class MapViewModelFactory private constructor(private val repository: MainRepository) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }

    companion object {
        @Volatile
        private var INSTANCE: MapViewModelFactory? = null

        fun getInstance(context: Context): MapViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val instance = MapViewModelFactory(Injector.injectAndProvideRepository(context))
                INSTANCE = instance
                instance
            }
        }
    }
}
