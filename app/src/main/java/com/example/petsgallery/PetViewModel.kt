package com.example.petsgallery


import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

open class PetViewModel(private val repository: PetRepository) : ViewModel() {
    sealed class PetImageState {
        object Loading : PetImageState()
        data class Success(val images: List<Image>) : PetImageState()
        data class Error(val exception: Throwable) : PetImageState()
    }

    private val _petImages = MutableLiveData<List<PetImage>>()
    val petImages: LiveData<List<PetImage>> get() = _petImages

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _searchText = MutableLiveData("")
    val searchText: LiveData<String> = _searchText

    private val _petImageState = MutableLiveData<PetImageState>(PetImageState.Loading)
    val petImageState: LiveData<PetImageState> = _petImageState


    private val _petImageUiState = MutableStateFlow<PetImageUiState>(PetImageUiState.Loading)
    open val petImageUiState: StateFlow<PetImageUiState> = _petImageUiState.asStateFlow()


    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()


    init {
        loadPetImages()
    }


    fun loadPetImages() {
        viewModelScope.launch {
            try {
                val images = repository.getPetImages()
                _petImages.postValue(images)
                _petImageUiState.value = PetImageUiState.Success(images)
            } catch (e: UnknownHostException) {
                // Handle specific case of no internet connection
                _petImageUiState.value = PetImageUiState.Error("No Internet Connection. Please try again later.")
            } catch (e: SocketTimeoutException) {
                // Handle specific case for request timeout
                _petImageUiState.value = PetImageUiState.Error("Request Timeout. Please try again.")
            } catch (e: Exception) {
                // Generic error handler for all other exceptions
                _petImageUiState.value = PetImageUiState.Error("Failed to load images: ${e.message}")
            }
        }
    }



    fun addPetImage(newPetImage: PetImage) {

        val currentList = _petImages.value ?: emptyList()


        val updatedList = currentList + newPetImage


        _petImages.postValue(updatedList)
    }

    fun addPetImage(uri: Uri, title: String, description: String, created: String) {

        val newPetImage = PetImage(
            url = uri.toString(),
            title = title,
            description = description,
            created = created
        )


        val currentList = _petImages.value ?: emptyList()
        _petImages.postValue(currentList + newPetImage)
    }


    fun onSearchTextChanged(newText: String) {
        _searchText.value = newText
        _petImageUiState.value = PetImageUiState.Success(
            _petImages.value?.filter {
                it.title.contains(newText, ignoreCase = true) ||
                        it.description.contains(newText, ignoreCase = true)
            } ?: emptyList()
        )
    }


    fun sortImages(ascending: Boolean) {
        val currentImages = _petImages.value ?: return
        val sortedImages = if (ascending) {
            currentImages.sortedBy { it.title }
        } else {
            currentImages.sortedByDescending { it.title }
        }
        _petImages.value = sortedImages
        _petImageUiState.value = PetImageUiState.Success(sortedImages)
    }


    private val _saveImageResult = MutableSharedFlow<SaveImageResult>()
    val saveImageResult = _saveImageResult.asSharedFlow()

    fun saveImage(context: Context, imageUrl: String) {
        viewModelScope.launch {
            if (hasWriteExternalStoragePermission(context)) {
                try {
                    saveImageFromUrlToGallery(context, imageUrl)
                    _saveImageResult.emit(SaveImageResult.Success)
                } catch (e: Exception) {
                    val errorMessage = when (e) {
                        is OutOfMemoryError -> "Insufficient memory to save image."
                        else -> "Failed to save image: ${e.message}"
                    }
                    _saveImageResult.emit(SaveImageResult.Failure(errorMessage))
                }
            } else {

                _saveImageResult.emit(SaveImageResult.Failure("Permission required to save image."))
            }
        }
    }

    sealed class SaveImageResult {
        object Success : SaveImageResult()
        data class Failure(val message: String) : SaveImageResult()
    }

    sealed class PetImageUiState {
        object Loading : PetImageUiState()
        data class Success(val images: List<PetImage>) : PetImageUiState()
        data class Error(val message: String) : PetImageUiState()
    }


    private fun hasWriteExternalStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private suspend fun saveImageFromUrlToGallery(context: Context, imageUrl: String) {
        try {
            val inputStream = URL(imageUrl).openStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            bitmap?.let {
                saveImageToGallery(context, bitmap)
            } ?: throw Exception("Failed to decode image.")
        } catch (e: Exception) {
            throw Exception("Failed to save image: ${e.message}")
        }
    }

    private fun saveImageToGallery(context: Context, bitmap: Bitmap) {
        val outputStream: OutputStream?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "Image_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            val imageUri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            outputStream = context.contentResolver.openOutputStream(imageUri!!)
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString()
            val image = File(imagesDir, "Image_${System.currentTimeMillis()}.jpg")
            outputStream = FileOutputStream(image)
        }

        outputStream?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            it.flush()
        }
    }
}