package com.msardevelopment.snob.data.api

import com.msardevelopment.snob.data.apimodel.NearbyPlacesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SnobApi {

    @GET("json")
    suspend fun getPlacesFromApi(@Query("location") location: String, @Query("radius") radius: Int, @Query("type") type: String, @Query("key") key: String): Response<NearbyPlacesResponse>

    @GET("json")
    suspend fun getPlacesFromApiNextPage(@Query("pagetoken") pagetoken: String, @Query("key") key: String): Response<NearbyPlacesResponse>

}