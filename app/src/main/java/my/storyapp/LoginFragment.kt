package my.storyapp

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import my.storyapp.customview.InterfaceToFragment
import my.storyapp.data.viewmodel.MainViewModel
import my.storyapp.data.viewmodel.MainViewModelFactory
import my.storyapp.databinding.FragmentLoginBinding

class LoginFragment : Fragment(), InterfaceToFragment {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewmodel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        val viewModelFactory = MainViewModelFactory.getInstance(requireContext())
        viewmodel = ViewModelProvider(requireActivity(), viewModelFactory)[MainViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val animationMoveHorizontal = ObjectAnimator.ofFloat(binding.loginIvBanner, View.TRANSLATION_X, -30f, 30f)
        animationMoveHorizontal.duration = 6000
        animationMoveHorizontal.repeatCount = ObjectAnimator.INFINITE
        animationMoveHorizontal.repeatMode = ObjectAnimator.REVERSE

        animationMoveHorizontal.start()

        if (LoginFragmentArgs.fromBundle(arguments as Bundle).token != "null") {
            navigateToHome()
        }
        else if (LoginFragmentArgs.fromBundle(arguments as Bundle).email != "null") {
            val email = LoginFragmentArgs.fromBundle(arguments as Bundle).email
            val password = LoginFragmentArgs.fromBundle(arguments as Bundle).password
            val loginLiveData = viewmodel.login(email, password)
            binding.buttonLogin.isEnabled = false
            binding.buttonLogin.text = ContextCompat.getString(requireContext(), R.string.status_loading)
            binding.loginProgressBar.visibility = View.VISIBLE
            binding.edLoginEmail.setText(email)
            binding.edLoginPassword.setText(password)

            loginLiveData.observe(viewLifecycleOwner) { result ->
                if (result != null && !result.error) {
                    viewmodel.setUser(result.loginResult)
                    navigateToHome()
                }
                else {
                    Snackbar.make(requireView(), result.message, Snackbar.LENGTH_LONG)
                        .setAction(ContextCompat.getString(requireContext(), R.string.action_try_again)) {
                            binding.buttonLogin.performClick()
                        }
                        .show()
                }

                validate()
                binding.loginProgressBar.visibility = View.GONE
            }
        }

        binding.edLoginEmail.registerInterfaceToFragment(this)
        binding.edLoginPassword.registerInterfaceToFragment(this)
        binding.buttonLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()
            val loginLiveData = viewmodel.login(email, password)
            val imm = requireActivity().getSystemService(InputMethodManager::class.java)

            imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
            binding.buttonLogin.isEnabled = false
            binding.buttonLogin.text = ContextCompat.getString(requireContext(), R.string.status_loading)
            binding.loginProgressBar.visibility = View.VISIBLE
            loginLiveData.observe(viewLifecycleOwner) { result ->
                if (result != null && !result.error) {
                    viewmodel.setUser(result.loginResult)
                    Snackbar.make(
                        requireView(),
                        ContextCompat.getString(requireContext(), R.string.snackbar_welcome) + ", ${result.loginResult.name}!",
                        Snackbar.LENGTH_SHORT)
                        .show()

                    navigateToHome()
                }
                else {
                    Snackbar.make(requireView(), result.message, Snackbar.LENGTH_LONG)
                        .setAction(ContextCompat.getString(requireContext(), R.string.action_try_again)) {
                            binding.buttonLogin.performClick()
                        }
                        .show()
                }

                validate()
                binding.loginProgressBar.visibility = View.GONE
            }
        }
    }

    private fun validate() {
        val isEmailValid = binding.edLoginEmail.isValid()
        val isPasswordValid = binding.edLoginPassword.isValid()

        if (isEmailValid && isPasswordValid) {
            binding.buttonLogin.isEnabled = true
            binding.buttonLogin.text = ContextCompat.getString(requireContext(), R.string.action_login)
        }
        else {
            binding.buttonLogin.isEnabled = false
            binding.buttonLogin.text = ContextCompat.getString(requireContext(), R.string.action_forbidden)
        }
    }

    private fun navigateToHome() {
        val action = LoginFragmentDirections.actionFragmentLoginToHomeFragment()
        findNavController().navigate(action)
    }

    override fun validateCustomEditText() {
        validate()
    }
}
