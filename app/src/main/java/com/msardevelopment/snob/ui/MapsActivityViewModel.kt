package com.msardevelopment.snob.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.msardevelopment.snob.Constants
import com.msardevelopment.snob.data.Resource
import com.msardevelopment.snob.data.domainmodel.Place
import com.msardevelopment.snob.data.repository.SnobRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapsActivityViewModel: ViewModel() {

    private val repository = SnobRepository()
    val placesLiveData = MutableLiveData<Resource<List<Place>>>()
    var mSelectedPlace: Place? = null
    var currentLatLng: LatLng? = null
    var currentZoomLevel: Float? = Constants.DEFAULT_ZOOM_LEVEL
    var currentPlaceType: String = Constants.PLACE_TYPE_DEFAULT

    fun getAllTypedPlaces(locationString: String, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            placesLiveData.postValue(Resource.Loading())
            val placesResource = repository.getAllTypedPlaces(locationString, type, false)
            placesLiveData.postValue(placesResource)
        }
    }
}