package my.storyapp.data.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import my.storyapp.data.Injector
import my.storyapp.data.MainRepository

class MainViewModelFactory private constructor(private val repository: MainRepository) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }

    companion object {
        @Volatile
        private var INSTANCE: MainViewModelFactory? = null

        fun getInstance(context: Context): MainViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val instance = MainViewModelFactory(Injector.injectAndProvideRepository(context))
                INSTANCE = instance
                instance
            }
        }
    }
}
