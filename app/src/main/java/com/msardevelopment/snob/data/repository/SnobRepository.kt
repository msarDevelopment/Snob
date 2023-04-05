package com.msardevelopment.snob.data.repository

import com.msardevelopment.snob.BuildConfig
import com.msardevelopment.snob.Constants
import com.msardevelopment.snob.data.Resource
import com.msardevelopment.snob.data.api.Retrofit
import com.msardevelopment.snob.data.domainmodel.Place
import com.msardevelopment.snob.data.apimodel.NearbyPlacesResponse
import retrofit2.Response

class SnobRepository {

    private val retrofit = Retrofit()
    private val api = retrofit.api

    suspend fun getAllTypedPlaces(locationString: String, type: String, isNextPage: Boolean): Resource<List<Place>> {
        val places: MutableList<Place> = mutableListOf()
        val placesResource = getTypedPlacesFromApi(locationString, type, isNextPage, places)
        return placesResource
    }

    private suspend fun getTypedPlacesFromApi(locationString: String, type: String, isNextPage: Boolean, places: MutableList<Place>): Resource<List<Place>> {

        var response: Response<NearbyPlacesResponse>? = null

        try {
            response = if(isNextPage) {
                api.getPlacesFromApiNextPage(locationString, BuildConfig.API_KEY)
            } else {
                api.getPlacesFromApi(locationString, Constants.RADIUS_PLACE_SEARCH, type, BuildConfig.API_KEY)
            }
        }
        catch (e: java.lang.Exception) {
            e.printStackTrace()
            return Resource.Error(e.message!!)
        }

        return if(response.isSuccessful && response.body() != null) {
            for (result: com.msardevelopment.snob.data.apimodel.Result in response.body()?.results!!) {
                if(result.rating >= Constants.MIN_RATING && result.userRatingsTotal >= Constants.MIN_NUMBER_OF_RATINGS && result.businessStatus == Constants.BUSINESS_STATUS_OPERATIONAL) {
                    val place = result.openingHours?.let {
                        Place(
                            result.name,
                            result.geometry.location.lng,
                            result.geometry.location.lat,
                            it.openNow,
                            result.rating,
                            result.types,
                            result.userRatingsTotal,
                            result.vicinity,
                            result.priceLevel
                        )
                    }
                    if (place != null) {
                        places.add(place)
                    }
                }
            }

            if(response.body()?.nextPageToken != null) {
                Thread.sleep(2000)
                getTypedPlacesFromApi(response.body()?.nextPageToken!!, type, true, places)
            }

            Resource.Success(places)
        } else {
            Resource.Error(response.message())
        }
    }
}