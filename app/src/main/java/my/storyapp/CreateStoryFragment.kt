package my.storyapp

import android.content.ContentValues
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import my.storyapp.data.retrofit.RegisterResponse
import my.storyapp.data.timeStamp
import my.storyapp.data.viewmodel.MainViewModel
import my.storyapp.data.viewmodel.MainViewModelFactory
import my.storyapp.databinding.FragmentCreateStoryBinding
import java.io.File

class CreateStoryFragment : Fragment() {
    private lateinit var binding: FragmentCreateStoryBinding
    private lateinit var viewmodel: MainViewModel
    private lateinit var currentImageUri: Uri
    private lateinit var fusedLocation: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private val intentPhotoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { result: Uri? ->
        if (result != null) {
            viewmodel.setCurrentImageUri(result)
        }
    }
    private val intentLaunchCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            viewmodel.setCurrentImageUri(currentImageUri)
        }
        else {
            Snackbar.make(
                requireView(),
                ContextCompat.getString(requireContext(), R.string.status_failed_to_take_picture),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
    private val requestLocationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        when {
            permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                getLastLocation()
            }
            permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                getLastLocation()
            }
            else -> {

            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreateStoryBinding.inflate(inflater, container, false)
        val viewModelFactory = MainViewModelFactory.getInstance(requireContext())
        viewmodel = ViewModelProvider(requireActivity(), viewModelFactory)[MainViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewmodel.currentImageUri.value != null) {
            binding.createStoryIvPicture.setImageURI(viewmodel.currentImageUri.value)
            binding.buttonAdd.isEnabled = true
        }
        else {
            binding.buttonAdd.isEnabled = false
        }

        binding.createStoryButtonGallery.setOnClickListener { v ->
            intentPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.createStoryButtonCamera.setOnClickListener { v ->
            currentImageUri = getImageUriForCamera()
            intentLaunchCamera.launch(currentImageUri)
        }
        binding.buttonAdd.setOnClickListener { v->
            binding.createStoryProgressBar.visibility = View.VISIBLE
            binding.buttonAdd.isEnabled = false

            val imageUri = viewmodel.currentImageUri.value
            lateinit var response: LiveData<RegisterResponse>

            if (imageUri != null) {
                if (binding.createStoryCbLocation.isChecked) {
                    response = viewmodel.postStory(
                        requireContext(),
                        binding.edAddDescription.text.toString(),
                        imageUri,
                        currentLocation?.latitude,
                        currentLocation?.longitude
                    )
                }
                else {
                    response = viewmodel.postStory(
                        requireContext(),
                        binding.edAddDescription.text.toString(),
                        imageUri
                    )
                }

                response.observe(viewLifecycleOwner) { result ->
                    binding.createStoryProgressBar.visibility = View.GONE
                    binding.buttonAdd.isEnabled = true

                    if (!result.error) {
                        viewmodel.setStoriesLoaded(false)
                        viewmodel.setShallUpdateAdapter(true)

                        val action =
                            CreateStoryFragmentDirections.actionFragmentCreateStoryToFragmentHome()

                        findNavController().navigate(action)
                    } else {
                        Snackbar.make(requireView(), ContextCompat.getString(requireContext(), R.string.status_failed_to_post_picture), Snackbar.LENGTH_SHORT)
                            .setAction(ContextCompat.getString(requireContext(), R.string.action_try_again)) {
                                binding.createStoryProgressBar.visibility = View.VISIBLE
                                binding.buttonAdd.isEnabled = false
                                binding.buttonAdd.performClick()
                            }
                            .show()
                    }
                }
            }
            else {
                Snackbar.make(
                    requireView(),
                    ContextCompat.getString(requireContext(), R.string.status_failed_to_take_picture),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        viewmodel.currentImageUri.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                binding.createStoryIvPicture.setImageURI(viewmodel.currentImageUri.value)
                binding.buttonAdd.isEnabled = true
            }
            else {
                binding.buttonAdd.isEnabled = false
            }
        }

        getLastLocation()
    }

    private fun getImageUriForCamera(): Uri {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$timeStamp.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/StoryApp/")
            }

            val uri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                return uri
            }
        }

        val filesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File(filesDir, "/StoryApp/$timeStamp.jpg")
        if (imageFile.parentFile?.exists() == false) imageFile.parentFile?.mkdir()
        return FileProvider.getUriForFile(
            requireContext(),
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            imageFile
        )
    }

    private fun checkLocationPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun getLastLocation() {
        binding.createStoryCbLocation.isEnabled = false

        if (checkLocationPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkLocationPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
            binding.createStoryCbLocation.isEnabled = true

            fusedLocation = LocationServices.getFusedLocationProviderClient(requireContext())
            fusedLocation.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLocation = location
                }
                else {
                    Snackbar.make(requireView(), R.string.status_failed_to_locate, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        else {
            requestLocationPermission.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}
