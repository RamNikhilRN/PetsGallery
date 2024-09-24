package com.example.petsgallery

import retrofit2.http.GET

interface PetApiService {
    @GET("/pets")
    suspend fun getPets(): List<PetImage>
}