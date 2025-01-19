package my.storyapp

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import my.storyapp.data.viewmodel.MainViewModel
import my.storyapp.data.viewmodel.MainViewModelFactory
import my.storyapp.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {
    private lateinit var binding: FragmentWelcomeBinding
    private lateinit var viewmodel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        val viewModelFactory = MainViewModelFactory.getInstance(requireContext())
        viewmodel = ViewModelProvider(requireActivity(), viewModelFactory)[MainViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val animationMoveHorizontal = ObjectAnimator.ofFloat(binding.welcomeIvBanner, View.TRANSLATION_X, -30f, 30f)
        animationMoveHorizontal.duration = 6000
        animationMoveHorizontal.repeatCount = ObjectAnimator.INFINITE
        animationMoveHorizontal.repeatMode = ObjectAnimator.REVERSE

        animationMoveHorizontal.start()
        binding.welcomeLogin.setOnClickListener { v ->
            val action = WelcomeFragmentDirections.actionFragmentWelcomeToFragmentLogin()
            findNavController().navigate(action)
        }
        binding.welcomeRegister.setOnClickListener { v->
            val action = WelcomeFragmentDirections.actionFragmentWelcomeToFragmentRegister()
            findNavController().navigate(action)
        }
        if (viewmodel.getUser().token.isNotEmpty()) {
            val action = WelcomeFragmentDirections.actionFragmentWelcomeToFragmentLogin()
            action.token = viewmodel.getUser().token
            findNavController().navigate(action)
        }
    }
}
