package my.storyapp.data.adapter

import android.view.View
import my.storyapp.data.retrofit.ListStoryItem

interface StoryListener {
    fun onItemClick(view: View?, adapterPosition: Int, storyItem: ListStoryItem)
}
