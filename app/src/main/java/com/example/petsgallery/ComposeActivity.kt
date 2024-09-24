package com.example.petsgallery

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ComposeActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = PetRepository(NetworkModule.petApiService)
        val factory = PetViewModelFactory(repository)

        setContent {
            var themeOption by remember { mutableStateOf(ThemeOption.SYSTEM) }
            val viewModel: PetViewModel = viewModel(factory = factory)


            AppTheme(themeOption = themeOption) {
                PetGalleryScreen(
                    viewModel = viewModel
                ) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    enum class ThemeOption { LIGHT, DARK, SYSTEM }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun PetGalleryScreen(
        viewModel: PetViewModel,
        onNavigateToXmlScreen: () -> Unit
    ) {
        var searchText by remember { mutableStateOf("") }
        var sortAscending by remember { mutableStateOf(true) }
        val uiState by viewModel.petImageUiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        var themeOption by remember { mutableStateOf(ThemeOption.SYSTEM) }


        LaunchedEffect(key1 = viewModel) {
            viewModel.saveImageResult.collect { result ->
                when (result) {
                    is PetViewModel.SaveImageResult.Success -> {
                        SnackbarDuration.Short
                        SnackbarResult.ActionPerformed

                    }

                    is PetViewModel.SaveImageResult.Failure -> {

                    }
                }
            }
        }

        Scaffold(
            topBar = {
                PetGalleryTopAppBar(onNavigateToXmlScreen, onThemeChange = { newTheme ->
                    themeOption = newTheme
                })
            }
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchBar(searchText) { newText ->
                    searchText = newText
                    viewModel.onSearchTextChanged(newText)
                }
                SortButtons(sortAscending) { newSortAscending ->
                    sortAscending = newSortAscending
                    viewModel.sortImages(newSortAscending)
                }
                UploadImageButton { uri, title, description, creationDate ->
                    uri?.let {
                        viewModel.addPetImage(
                            it,
                            title,
                            description,
                            creationDate
                        )
                    }
                }


                when (uiState) {
                    is PetViewModel.PetImageUiState.Loading -> LoadingIndicator()
                    is PetViewModel.PetImageUiState.Success -> {
                        ImageList(
                            images = (uiState as PetViewModel.PetImageUiState.Success).images,
                            onSaveClick = { petImage ->
                                coroutineScope.launch {
                                    viewModel.saveImage(context, petImage.url)
                                }
                            }
                        )
                    }

                    is PetViewModel.PetImageUiState.Error -> ErrorMessage((uiState as PetViewModel.PetImageUiState.Error).message)
                }
            }
        }
    }

    @Composable
    fun SearchBar(searchText: String, onSearchTextChanged: (String) -> Unit) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { onSearchTextChanged(it) },
            label = { Text("Search by title or description") },
            modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    fun SortButtons(sortAscending: Boolean, onSortChange: (Boolean) -> Unit) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { onSortChange(true) }) {
                Text("A-Z")
            }
            Button(onClick = { onSortChange(false) }) {
                Text("Z-A")
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PetGalleryTopAppBar(
        onNavigateToXmlScreen: () -> Unit,
        onThemeChange: (ThemeOption) -> Unit
    ) {
        var showMenu by remember { mutableStateOf(false) }

        TopAppBar(
            title = { Text("Pet Gallery - Compose") },
            actions = {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Light Theme") },
                        onClick = {
                            onThemeChange(ThemeOption.LIGHT)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Dark Theme") },
                        onClick = {
                            onThemeChange(ThemeOption.DARK)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("System Default") },
                        onClick = {
                            onThemeChange(ThemeOption.SYSTEM)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Switch to XML UI") },
                        onClick = {
                            onNavigateToXmlScreen()
                            showMenu = false
                        }
                    )
                }
            }
        )
    }


    @Composable
    fun UploadImageButton(onImagePicked: (Uri?, String, String, String) -> Unit) {
        val context = LocalContext.current
        var showDialog by remember { mutableStateOf(false) }
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var selectedUri by remember { mutableStateOf<Uri?>(null) }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri: Uri? ->
                if (uri != null) {
                    selectedUri = uri
                    showDialog = true
                }
            }
        )

        Button(onClick = {
            launcher.launch("image/*")
        }) {
            Text("Upload Image")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    Button(onClick = {
                        if (title.isNotEmpty() && description.isNotEmpty() && selectedUri != null) {
                            val creationDate =
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                    .format(java.util.Date())


                            onImagePicked(selectedUri, title, description, creationDate)

                            showDialog = false
                            title = ""
                            description = ""
                            selectedUri = null
                        } else {
                            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }) {
                        Text("Upload")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Enter Image Details") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }
    }

    @Composable
    fun ImageList(images: List<PetImage>, onSaveClick: (PetImage) -> Unit) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(images) { pet ->
                PetImageCard(pet = pet, onSaveClick = { onSaveClick(pet) })
            }
        }
    }

    @Composable
    fun PetImageCard(pet: PetImage, onSaveClick: () -> Unit) {
        val context = LocalContext.current

        var showDialog by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = pet.title, style = MaterialTheme.typography.titleLarge)
                Text(text = pet.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = pet.url,
                    contentDescription = pet.title,
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "Created on: ${pet.created}",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { showDialog = true }) {
                    Text("Save Image")
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        confirmButton = {
                            Button(onClick = {
                                onSaveClick()
                                showDialog = false
                            }) {
                                Text("Confirm")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showDialog = false }) {
                                Text("Cancel")
                            }
                        },
                        title = { Text("Save Image") },
                        text = { Text("Are you sure you want to save this image?") }
                    )
                }
            }
        }
    }

    @Composable
    fun LoadingIndicator() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    @Composable
    fun ErrorMessage(message: String) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }
    }

    @Composable
    fun AppTheme(
        themeOption: ThemeOption,
        content: @Composable () -> Unit
    ) {
        val colorScheme = when (themeOption) {
            ThemeOption.DARK -> darkColorScheme()
            ThemeOption.LIGHT -> lightColorScheme()
            ThemeOption.SYSTEM -> if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
        }

        MaterialTheme(colorScheme = colorScheme) {
            content()
        }
    }
}