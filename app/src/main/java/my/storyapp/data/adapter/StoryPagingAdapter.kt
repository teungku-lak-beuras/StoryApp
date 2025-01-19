package my.storyapp.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import my.storyapp.R
import my.storyapp.data.retrofit.ListStoryItem
import my.storyapp.databinding.ItemRowStoryBinding

class StoryPagingAdapter(private val storyListener: StoryListener) : PagingDataAdapter<ListStoryItem, StoryPagingAdapter.StoryViewHolder>(DIFF_CALLBACK) {
    class StoryViewHolder(val binding: ItemRowStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(storyItem: ListStoryItem) {
            Glide.with(itemView.context)
                .load(storyItem.photoUrl)
                .apply(RequestOptions.placeholderOf(R.drawable.baseline_cloud_download_24).error(R.drawable.baseline_cloud_off_24))
                .into(binding.tvItemPhoto)
            binding.tvItemName.text = storyItem.name
            binding.itemRowStoryDescription.text = storyItem.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemRowStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)

        story?.let { holder.bind(it) }
        holder.binding.root.setOnClickListener { view ->
            if (story != null) {
                storyListener.onItemClick(view, holder.bindingAdapterPosition, story)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ListStoryItem> = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
