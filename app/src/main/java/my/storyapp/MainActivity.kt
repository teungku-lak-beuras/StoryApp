package my.storyapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import my.storyapp.data.viewmodel.MainViewModel
import my.storyapp.data.viewmodel.MainViewModelFactory
import my.storyapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewmodel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory = MainViewModelFactory.getInstance(this)
        viewmodel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        setup()
    }

    private fun setup() {
        val navController = findNavController(R.id.home_fragment_container)

        navController.addOnDestinationChangedListener{ controller, destination, arguments ->
            when (destination.id) {
                R.id.fragment_home -> {
                    binding.mainToolbar.title = ContextCompat.getString(this, R.string.app_name)
                    binding.mainToolbar.visibility = View.VISIBLE
                    binding.mainToolbar.navigationIcon = null
                    binding.mainToolbar.setNavigationOnClickListener {
                        navController.navigateUp()
                    }
                }

                R.id.fragment_detail -> {
                    binding.mainToolbar.title = ContextCompat.getString(this, R.string.detail_title)
                    binding.mainToolbar.visibility = View.VISIBLE
                    binding.mainToolbar.setNavigationIcon(R.drawable.ic_close_black_24dp)
                }

                else -> {
                    binding.mainToolbar.visibility = View.GONE
                }
            }
        }
        binding.mainToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    viewmodel.deleteUser()
                    navController.navigate(
                        R.id.fragment_welcome,
                        null,
                        NavOptions.Builder().setPopUpTo(R.id.fragment_home,
                            inclusive = true,
                            saveState = false
                        ).build()
                    )
                }

                R.id.action_map -> {
                    val intent = Intent(this@MainActivity, MapActivity::class.java)

                    startActivity(intent)
                }
            }

            true
        }
    }
}
