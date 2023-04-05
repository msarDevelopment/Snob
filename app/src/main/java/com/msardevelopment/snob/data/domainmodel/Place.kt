package com.msardevelopment.snob.data.domainmodel

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class Place(
    val name: String,
    val lng: Double,
    val lat: Double,
    val openNow: Boolean,
    val rating: Double,
    val types: List<String>,
    val userRatingsTotal: Int,
    val vicinity: String,
    val priceLevel: Int?
    ) : ClusterItem {
    override fun getPosition(): LatLng {
        return LatLng(lat, lng)
    }

    override fun getTitle(): String {
        return name
    }

    override fun getSnippet(): String {
        return vicinity
    }

    override fun getZIndex(): Float? {
        return 1.0f
    }
}
