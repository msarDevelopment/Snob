package com.msardevelopment.snob.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.maps.android.clustering.ClusterManager
import com.msardevelopment.snob.Constants
import com.msardevelopment.snob.R
import com.msardevelopment.snob.data.Resource
import com.msardevelopment.snob.data.domainmodel.Place
import com.msardevelopment.snob.databinding.ActivityMapsBinding
import kotlin.math.round

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var mapsActivityViewModel: MapsActivityViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var clusterManager: ClusterManager<Place>
    private lateinit var placeIconRenderer: PlaceIconRenderer
    private var mSelectedMarker: Marker? = null
    private lateinit var imageBitmapNormal: Bitmap
    private lateinit var imageBitmapActive: Bitmap
    private var mBottomSheetBehavior: BottomSheetBehavior<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapsActivityViewModel = ViewModelProvider(this)[MapsActivityViewModel::class.java]

        imageBitmapNormal = BitmapFactory.decodeResource(resources, R.drawable.ic_pin_snob_normal)
        imageBitmapActive = BitmapFactory.decodeResource(resources, R.drawable.ic_pin_snob_active)

        mBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        binding.ivClose.setOnClickListener {
            //all that is needed is to change state, addBottomSheetCallback will take care
            //of changing icon and unselecting marker in mapsActivityViewModel and clusterManager.renderer
            mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        }

        getLocationPermission()
    }

    private fun getLocationPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initMap()
        }
        else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                initMap()
            }
        }
    }

    private fun initMap(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        setLocation()

        setUpClusterManager(mMap)

        addBottomSheetOnCloseBehavior()

        //to keep bottom sheet open after rotation
        if(mapsActivityViewModel.mSelectedPlace != null) {
            updateBottomSheetContent(mapsActivityViewModel.mSelectedPlace!!)
        }

        mMap.setOnMapClickListener {
            //all that is needed is to change state, addBottomSheetCallback will take care
            //of changing icon and unselecting marker in mapsActivityViewModel and clusterManager.renderer
            mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        }

        startObserving()

        setUpButtons()
    }

    private fun setLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getDeviceLocation()
        }
        else {
            if(mapsActivityViewModel.currentLatLng == null){
                //to set location only on the app start, during app usage it will be set
                //based on user's interaction
                mapsActivityViewModel.currentLatLng = LatLng(
                    Constants.DEFAULT_LAT,
                    Constants.DEFAULT_LNG
                )
            }
            moveCameraToCurrentLocation()
        }
    }

    //is called when map is ready so mMap can safely be used inside this method
    private fun getDeviceLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
                if(location != null) {
                    //to set location only on the app start, during app usage it will be set
                    //based on user's interaction
                    if(mapsActivityViewModel.currentLatLng == null){
                        mapsActivityViewModel.currentLatLng = LatLng(location.latitude, location.longitude)
                    }
                    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mMap.isMyLocationEnabled = true
                        moveCameraToCurrentLocation()
                    }
                    else if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mMap.addCircle(CircleOptions()
                            .center(LatLng(location.latitude, location.longitude))
                            .radius(Constants.RADIUS_COARSE_LOCATION_DRAW)
                            .strokeColor(0x220000FF)
                            .fillColor(0x220000FF))
                        moveCameraToCurrentLocation()
                    }
                }
            }
        }
    }

    private fun moveCameraToCurrentLocation() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mapsActivityViewModel.currentLatLng!!,mapsActivityViewModel.currentZoomLevel!!), 2000, object : CancelableCallback {
            override fun onFinish() {
                //to set places only on the app start, during app usage they will be set
                //based on user's interaction
                if(mapsActivityViewModel.placesLiveData.value?.data == null) {
                    loadPlaces(Constants.PLACE_TYPE_DEFAULT)
                }
            }
            override fun onCancel() {}
        })
    }

    private fun setUpClusterManager(mMap: GoogleMap) {
        clusterManager = ClusterManager(this, mMap)

        mMap.setOnCameraIdleListener(clusterManager)

        placeIconRenderer = PlaceIconRenderer(this, mMap, clusterManager, mapsActivityViewModel.mSelectedPlace)
        clusterManager.renderer = placeIconRenderer

        clusterManager.setOnClusterItemClickListener { place ->

            if(mapsActivityViewModel.mSelectedPlace != null) {
                val lastMarker = placeIconRenderer.getMarker(mapsActivityViewModel.mSelectedPlace)
                lastMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(imageBitmapNormal))
            }

            mapsActivityViewModel.mSelectedPlace = place

            if(place != null) {
                mSelectedMarker = placeIconRenderer.getMarker(place)
                mSelectedMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(imageBitmapActive))
                //to remember selected Place on rotation or reclustering
                with(clusterManager.renderer as PlaceIconRenderer) {
                    this.selectedPlace = place
                }
            }

            updateBottomSheetContent(place)

            mapsActivityViewModel.currentLatLng = LatLng(place.lat, place.lng)

            true
        }
    }

    private fun updateBottomSheetContent(place: Place) {
        if(mapsActivityViewModel.currentPlaceType == Constants.PLACE_TYPE_RESTAURANT) {
            binding.ivBusiness.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_business_restaurant))
        }
        else {
            binding.ivBusiness.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_business_cafe))
        }

        binding.tvBusiness.text = place.name
        binding.tvAddress.text = place.vicinity
        binding.tvRating.text = place.rating.toString()
        binding.tvNumberOfRatings.text = place.userRatingsTotal.toString()

        binding.btnRoute.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${place.lat},${place.lng} (${place.vicinity})"))
            startActivity(intent)
        }
        mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun addBottomSheetOnCloseBehavior() {
        mBottomSheetBehavior?.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    //check to avoid crash in scenario:
                    //open Place details
                    //move on map so that marker disappears
                    //close details - without checking this it would crash here with Unmanaged descriptor
                    if (mapsActivityViewModel.mSelectedPlace != null && clusterManager.renderer != null) {
                        //change marker icon to normal when unselecting
                        val lastMarker = placeIconRenderer.getMarker(mapsActivityViewModel.mSelectedPlace)
                        lastMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(imageBitmapNormal))
                        //to unselect selected Place so it doesn't reappear on rotation or reclustering
                        mapsActivityViewModel.mSelectedPlace = null
                        with(clusterManager.renderer as PlaceIconRenderer) {
                            this.selectedPlace = null
                        }
                    }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun startObserving() {
        mapsActivityViewModel.placesLiveData.observe(this) {
            when(it) {
                is Resource.Success -> {
                    hideLoadingAnimation()
                    if (it.data != null) {
                        clusterManager.clearItems()
                        for(place in it.data){
                            clusterManager.addItem(place)
                        }
                        //Toast.makeText(this,"Places loaded: ${it.data.size}", Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, resources.getString(R.string.number_of_places_loaded, it.data.size), Toast.LENGTH_SHORT).show()
                        clusterManager.cluster()
                    }
                }
                is Resource.Error -> {
                    hideLoadingAnimation()
                    if (it.message != null)
                        Toast.makeText(this,"${it.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    showLoadingAnimation()
                }
            }
        }
    }

    private fun showLoadingAnimation() {
        binding.avLoadingAnimation.visibility = View.VISIBLE
    }

    private fun hideLoadingAnimation() {
        binding.avLoadingAnimation.visibility = View.GONE
    }

    private fun setUpButtons() {
        binding.segBtnType.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.btnRestaurants) {
                    mapsActivityViewModel.currentPlaceType = Constants.PLACE_TYPE_RESTAURANT
                    loadPlaces(mapsActivityViewModel.currentPlaceType)
                }
                if (checkedId == R.id.btnCafe) {
                    mapsActivityViewModel.currentPlaceType = Constants.PLACE_TYPE_CAFE
                    loadPlaces(mapsActivityViewModel.currentPlaceType)
                }
            }
        }

        binding.btnSearchThisArea.setOnClickListener {
            loadPlaces(mapsActivityViewModel.currentPlaceType)
        }
    }

    private fun loadPlaces(type: String) {
        val curScreen: LatLngBounds =  mMap.projection.visibleRegion.latLngBounds
        val locationString = "${curScreen.center.latitude},${curScreen.center.longitude}"

        if(isNetworkConnected()) {
            mapsActivityViewModel.getAllTypedPlaces(locationString, type)
        }
        else {
            Toast.makeText(this, R.string.no_internet_message, Toast.LENGTH_SHORT).show()
        }

        mapsActivityViewModel.currentLatLng = curScreen.center
        mapsActivityViewModel.currentZoomLevel = mMap.cameraPosition.zoom
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}