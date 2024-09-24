# PetsGallery

PetsGallery is an Android application designed to fetch and display a list of pet images. Users can view, sort, search, upload, and save images from their gallery. This project demonstrates the usage of Jetpack Compose, XML-based UI, API integration, and various Android components.

## Key Features
- Display pet images fetched from a remote API.
- Async image loading with caching.
- Sort images in ascending (A-Z) or descending (Z-A) order.
- Search images by title or description.
- Upload images from the user's gallery.
- Save images locally on the device.
- Light, Dark, and System theming options.
- Transition from Compose-based UI to XML-based UI.
- Error handling for failed requests.
- Unit and UI testing to ensure code reliability.

## Core Components

### `ComposeActivity.kt`
- Manages the entry point of your app.
- Sets up a `ViewModel` and uses `PetGalleryScreen` as the primary UI composable.
- Uses `Scaffold` for a basic app structure with a top app bar and body content.

### `MainActivity.kt`
- It uses the onCreate() method to set the content using Jetpack Compose (setContent).
- Likely handles a different screen activity.
- Possibly integrates with `ComposeActivity.kt` for navigation between a compose-based screen and an XML-based UI screen.

### `NetworkModule.kt`
- Configures the network API service using libraries like Retrofit.
- Enables communication with the backend server to retrieve pet images and other relevant data.

### `PetApiService.kt`
- Defines the API endpoints for interacting with the server's pet image resources (retrieving, uploading).

### `PetImage.kt`
- Data model class representing a pet image with attributes like title, description, URL, and creation date.

### `PetImageAdapter.kt`
- Manages the display of pet images in a list or gallery.
- Likely handles data binding for each pet image item in a `RecyclerView` or `LazyColumn`.

### `PetRepository.kt`
- Contains business logic and manages data fetching from the API through `PetApiService`.
- Provides the data to the `ViewModel`.

### `PetViewModel.kt`
- Acts as a middleman between the repository and the UI.
- Handles UI states (loading, success, error).
- Performs operations like saving or fetching images.

### `PetViewModelFactor.kt`
- Factory class used for creating an instance of `PetViewModel` with its dependencies (like `PetRepository`).

## XML Layouts

### `activity_main.xml`
- Defines the main layout of the activity.
- Includes:
  - A search bar for filtering images by title or description.
  - Buttons for sorting images and uploading an image.
  - A `RecyclerView` to display images in a list.
  
### `dialog_image_upload.xml`
- Defines the layout for the dialog used to upload an image.
- Includes input fields for image title and description.

### `item_pet_image.xml`
- Defines the layout for each item in the `RecyclerView`.
- Displays an image, title, description, creation date, and a button to save the image.

## Key Functionalities

### 1. **Display a List of Images:**
   - Fetches images using a GET request from `https://eulerity-hackathon.appspot.com/pets`.
   - Displays images and associated data asynchronously.

### 2. **Sorting:**
   - Allows sorting images in ascending (A-Z) or descending (Z-A) order.

### 3. **Search Functionality:**
   - Filters displayed images based on title or description.

### 4. **Upload Image from User's Gallery:**
   - Enables the user to upload an image with a title and description.

### 5. **Save Image Locally:**
   - Allows the user to save an image locally to the device.

### 6. **UI in Compose & XML:**
   -Easy UI state change from XML-based UI to Compose-based UI  

### 7. **Theme Changing:**
   - Dark, Light, and System theme support.


## Testing

### `PetViewModelTest.kt`
This file focuses on testing the `PetViewModel`, which handles the business logic of the app.

- **LiveData/StateFlow Testing:** Uses `InstantTaskExecutorRule` to ensure LiveData updates happen immediately.
- **Test 1: Success State**
   - Mocks `PetRepository` to return a predefined list of `PetImage` objects.
   - Verifies that the `PetViewModel` updates to Success state.
- **Test 2: Error State**
   - Mocks the repository to throw an exception when fetching images.
   - Verifies that the `ViewModel` emits an Error state with an appropriate message.

### `PetGalleryUiTest.kt`
This file tests the UI components of the app, specifically buttons and interactions in the pet gallery.

- **Compose UI Test:** Uses `createAndroidComposeRule<ComponentActivity>()` for testing the Compose UI.
- **Test: Button Interactions**
   - Tests that the sorting buttons correctly trigger callbacks to change the sort order.

## Screenshots

1. **Sort Ascending**
   ![Sort Ascending](https://github.com/user-attachments/assets/17752f90-6fac-44b3-af47-0b6b430e0cf6)

2. **Sort Descending**
   ![Sort Descending](https://github.com/user-attachments/assets/2691107a-26e2-4417-86f2-f1de94ddb4c6)

3. **Upload Image from Gallery**
   ![Upload Image](https://github.com/user-attachments/assets/36367297-61ac-4639-a399-c05774e736c3)

4. **Save Image**
   ![Save Image](https://github.com/user-attachments/assets/4045e8d4-31db-46dd-b44f-5944d8383775)

5. **Async Search**
   ![Async Search](https://github.com/user-attachments/assets/659af55f-459c-412c-ba04-85d30e33d20e)

6. **Choose Themes**
   ![Choose Themes](https://github.com/user-attachments/assets/66e39f3e-f322-4446-a208-0592f53c3c91)

7. **Dark Theme**
   ![Dark Theme](https://github.com/user-attachments/assets/4d5955fb-713a-4728-a087-9bb0b896ef36)

## Project Setup

### Prerequisites
- **Android Studio**: (Latest stable version)
  - I have used **Android Studio Koala Feature Drop | 2024.1.2**.
- **Minimum SDK**: 24
- **Target SDK**: 34


## API
The app fetches data from the following public API:
- [Eulerity API](https://eulerity-hackathon.appspot.com/pets)

