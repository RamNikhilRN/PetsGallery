package com.example.petsgallery


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PetViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var viewModel: PetViewModel
    private val repository: PetRepository = mockk()

    @Before
    fun setup() = runTest {
        Dispatchers.setMain(testDispatcher)
        viewModel = PetViewModel(repository)
    }

    @Test
    fun `loadPetImages emits success when repository returns data`() = runTest {
        val sampleData = listOf(
            PetImage(
                url = "https://images.pexels.com/photos/2607544/pexels-photo-2607544.jpeg?format=tiny",
                title = "Barky Spears",
                description = "Woof! I did it again",
                created = "Sun Sep 22 21:51:46 UTC 2024"
            )
        )

        coEvery { repository.getPetImages() } returns sampleData

        viewModel.loadPetImages()
        testDispatcher.scheduler.advanceUntilIdle()

        assert(viewModel.petImageUiState.value is PetViewModel.PetImageUiState.Success)
        val actualUiState = viewModel.petImageUiState.value as PetViewModel.PetImageUiState.Success
        assertEquals(sampleData, actualUiState.images)
    }

    @Test
    fun `loadPetImages emits error when repository throws exception`() = runTest {
        coEvery { repository.getPetImages() } throws PetImageLoadingException("Error loading images")

        viewModel.loadPetImages()
        testDispatcher.scheduler.advanceUntilIdle()

        assert(viewModel.petImageUiState.value is PetViewModel.PetImageUiState.Error)
        val actualUiState = viewModel.petImageUiState.value as PetViewModel.PetImageUiState.Error
        assertEquals("Failed to load images: Error loading images", actualUiState.message)
    }
}

class PetImageLoadingException(message: String) : Exception(message)

