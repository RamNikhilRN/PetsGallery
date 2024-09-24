package com.example.petsgallery

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val BASE_URL = "https://eulerity-hackathon.appspot.com/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val petApiService: PetApiService by lazy {
        retrofit.create(PetApiService::class.java)
    }
}
