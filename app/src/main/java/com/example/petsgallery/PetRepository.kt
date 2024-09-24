package com.example.petsgallery


open class PetRepository(private val petApiService: PetApiService) {

    // Function to fetch images from the API
    open suspend fun getPetImages(): List<PetImage> {
        return petApiService.getPets()  // Calls the API service
    }
}
