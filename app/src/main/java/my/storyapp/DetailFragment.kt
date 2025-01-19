package my.storyapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import my.storyapp.data.viewmodel.MainViewModel
import my.storyapp.data.viewmodel.MainViewModelFactory
import my.storyapp.databinding.FragmentDetailBinding

class DetailFragment : Fragment() {
    private lateinit var binding: FragmentDetailBinding
    private lateinit var viewmodel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        val viewModelFactory = MainViewModelFactory.getInstance(requireContext())
        viewmodel = ViewModelProvider(requireActivity(), viewModelFactory)[MainViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val storyItem = DetailFragmentArgs.fromBundle(arguments as Bundle).storyItem

        Glide.with(requireContext())
            .load(storyItem.photoUrl)
            .apply(RequestOptions.placeholderOf(R.drawable.baseline_cloud_download_24).error(R.drawable.baseline_cloud_off_24))
            .into(binding.tvDetailPhoto)
        binding.tvDetailName.text = storyItem.name
        binding.tvDetailDescription.text = storyItem.description
    }
}
