package my.storyapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import my.storyapp.data.adapter.StoryListener
import my.storyapp.data.adapter.StoryPagingAdapter
import my.storyapp.data.adapter.StoryStateAdapter
import my.storyapp.data.retrofit.ListStoryItem
import my.storyapp.data.viewmodel.MainViewModel
import my.storyapp.data.viewmodel.MainViewModelFactory
import my.storyapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment(), StoryListener {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewmodel: MainViewModel
    private lateinit var adapter: StoryPagingAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val viewModelFactory = MainViewModelFactory.getInstance(requireContext())
        viewmodel = ViewModelProvider(requireActivity(), viewModelFactory)[MainViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = StoryPagingAdapter(this)
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView = binding.homeRecyclerView
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter.withLoadStateFooter(footer = StoryStateAdapter { adapter.retry() })
        recyclerView.visibility = View.GONE
        binding.homeProgressBar.visibility = View.VISIBLE

        viewmodel.getPagingStories().observe(viewLifecycleOwner) { result ->
            adapter.submitData(lifecycle, result)

            recyclerView.visibility = View.VISIBLE
            binding.homeProgressBar.visibility = View.GONE
        }
        binding.homeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    binding.homeFloatingButton.visibility = View.GONE
                }
                else {
                    binding.homeFloatingButton.visibility = View.VISIBLE
                }
            }
        })
        binding.homeFloatingButton.setOnClickListener { v ->
            val action = HomeFragmentDirections.actionFragmentHomeToFragmentCreateStory()
            findNavController().navigate(action)
        }
    }

    override fun onItemClick(view: View?, adapterPosition: Int, storyItem: ListStoryItem) {
        val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(storyItem)
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()

        if (viewmodel.shallUpdateAdapter()) {
            adapter.refresh()
            recyclerView.smoothScrollToPosition(0)
            viewmodel.setShallUpdateAdapter(false)
        }
    }
}
