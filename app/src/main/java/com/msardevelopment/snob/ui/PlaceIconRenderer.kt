package com.msardevelopment.snob.ui

import android.content.Context
import android.graphics.BitmapFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.msardevelopment.snob.R
import com.msardevelopment.snob.data.domainmodel.Place

class PlaceIconRenderer(
    val context: Context?,
    map: GoogleMap?,
    clusterManager: ClusterManager<Place>?,
    var selectedPlace: Place?
) : DefaultClusterRenderer<Place>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(item: Place, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        val imageBitmap = if(item == selectedPlace) BitmapFactory.decodeResource(context?.resources,
            R.drawable.ic_pin_snob_active
        ) else BitmapFactory.decodeResource(context?.resources, R.drawable.ic_pin_snob_normal)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(imageBitmap))
    }
}