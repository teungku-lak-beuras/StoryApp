package my.storyapp

import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import my.storyapp.data.viewmodel.MapViewModel
import my.storyapp.data.viewmodel.MapViewModelFactory
import my.storyapp.databinding.ActivityMapBinding

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapBinding
    private lateinit var viewmodel: MapViewModel
    private val boundsBuilder = LatLngBounds.Builder()
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory = MapViewModelFactory.getInstance(this)
        viewmodel = ViewModelProvider(this, viewModelFactory)[MapViewModel::class.java]

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val jakarta = LatLng(-6.174709277454876, 106.8270437942654)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(jakarta, 15f))
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.setOnMapLongClickListener { latLng ->
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("New Marker")
                    .snippet("Lat: ${latLng.latitude} Long: ${latLng.longitude}")
                    .icon(vectorToBitmap(R.drawable.baseline_location_searching_24, Color.parseColor("#FF00FF")))
            )
        }
        mMap.setOnPoiClickListener { pointOfInterest ->
            mMap.addMarker(
                MarkerOptions()
                    .position(pointOfInterest.latLng)
                    .title(pointOfInterest.name)
                    .icon(vectorToBitmap(R.drawable.baseline_location_searching_24, Color.parseColor("#FF00FF")))
            )
        }
        setMapStyle()
        getMyLocation()
        getLocatedStories()
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getLocatedStories() {
        viewmodel.locatedStories.observe(this) { result ->
            Snackbar.make(binding.root, "Memuat cerita", Snackbar.LENGTH_SHORT).show()

            if (result != null && !result.error) {
                Snackbar.make(binding.root, "Sukses?", Snackbar.LENGTH_SHORT).show()
                result.listStory.forEach { item ->
                    val latlng = LatLng(item.lat, item.lon)

                    mMap.addMarker(MarkerOptions()
                        .position(latlng)
                        .title(item.name)
                        .snippet(item.description)
                        .icon(vectorToBitmap(R.drawable.baseline_location_searching_24, ContextCompat.getColor(this, R.color.my_magenta)))
                    )

                    // Indonesia only to the bounds!
                    if (latlng.latitude < 7 && latlng.latitude > -12 && latlng.longitude > 94 && latlng.longitude < 141) {
                        boundsBuilder.include(latlng)
                    }
                }

                val bounds: LatLngBounds = boundsBuilder.build()

                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        bounds,
                        resources.displayMetrics.widthPixels,
                        resources.displayMetrics.heightPixels,
                        300
                    )
                )
            }
            else {
                Snackbar.make(binding.root, R.string.status_failed_to_take_story, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.action_try_again) {
                        viewmodel.getLocatedStories(this)
                    }
                    .show()
            }
        }
    }

    private fun vectorToBitmap(@DrawableRes id: Int, @ColorInt color: Int): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
        if (vectorDrawable == null) {
            Log.e("BitmapHelper", "Resource not found")
            return BitmapDescriptorFactory.defaultMarker()
        }
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, color)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private companion object {
        private const val TAG = "MapActivity"
    }
}
