package my.storyapp.data.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import my.storyapp.databinding.ItemPagingLoadStateBinding

class StoryStateAdapter (private val retry: () -> Unit) : LoadStateAdapter<StoryStateAdapter.ViewHolder>() {
    class ViewHolder(private val binding: ItemPagingLoadStateBinding, retry: () -> Unit) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.loadStateButtonTryAgain.setOnClickListener {
                retry.invoke()
            }
        }

        fun bind(loadState: LoadState) {
            if (loadState is LoadState.Error) {
                binding.loadStateTvStatus.text = loadState.error.localizedMessage
            }

            binding.loadStateProgressBar.isVisible = loadState is LoadState.Loading
            binding.loadStateButtonTryAgain.isVisible = loadState is LoadState.Error
            binding.loadStateTvStatus.isVisible = loadState is LoadState.Error
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        val binding = ItemPagingLoadStateBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding, retry)
    }
}
