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
import my.storyapp.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment(), InterfaceToFragment {
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var viewmodel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val viewModelFactory = MainViewModelFactory.getInstance(requireContext())
        viewmodel = ViewModelProvider(requireActivity(), viewModelFactory)[MainViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val animationMoveHorizontal = ObjectAnimator.ofFloat(binding.registerIvBanner, View.TRANSLATION_X, -30f, 30f)
        animationMoveHorizontal.duration = 6000
        animationMoveHorizontal.repeatCount = ObjectAnimator.INFINITE
        animationMoveHorizontal.repeatMode = ObjectAnimator.REVERSE

        animationMoveHorizontal.start()

        binding.edRegisterName.registerInterfaceToFragment(this)
        binding.edRegisterEmail.registerInterfaceToFragment(this)
        binding.edRegisterPassword.registerInterfaceToFragment(this)
        binding.buttonRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()
            val registerLiveData = viewmodel.register(name, email, password)
            val imm = requireActivity().getSystemService(InputMethodManager::class.java)

            imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
            binding.buttonRegister.isEnabled = false
            binding.buttonRegister.text = ContextCompat.getString(requireContext(), R.string.status_loading)
            binding.registerProgressBar.visibility = View.VISIBLE
            registerLiveData.observe(viewLifecycleOwner) { result ->
                if (result != null && !result.error) {
                    Snackbar.make(
                        requireView(),
                        ContextCompat.getString(requireContext(), R.string.status_register_success),
                        Snackbar.LENGTH_LONG)
                        .show()

                    val action = RegisterFragmentDirections.actionFragmentRegisterToFragmentLogin()
                    action.email = email
                    action.password = password

                    findNavController().navigate(action)
                }
                else {
                    Snackbar.make(requireView(), result.message, Snackbar.LENGTH_LONG)
                        .setAction(ContextCompat.getString(requireContext(), R.string.action_try_again)) {
                            binding.buttonRegister.performClick()
                        }
                        .show()
                }

                validate()
                binding.registerProgressBar.visibility = View.GONE
            }
        }
    }

    override fun validateCustomEditText() {
        validate()
    }

    private fun validate() {
        val isNameNotEmpty = binding.edRegisterName.isValid()
        val isEmailValid = binding.edRegisterEmail.isValid()
        val isPasswordValid = binding.edRegisterPassword.isValid()

        if (isNameNotEmpty && isEmailValid && isPasswordValid) {
            binding.buttonRegister.isEnabled = true
            binding.buttonRegister.text = ContextCompat.getString(requireContext(), R.string.action_register)
        }
        else {
            binding.buttonRegister.text = ContextCompat.getString(requireContext(), R.string.action_forbidden)
        }
    }
}
