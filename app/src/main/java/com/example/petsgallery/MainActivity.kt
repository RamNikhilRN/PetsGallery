package com.example.petsgallery

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {


    private lateinit var viewModel: PetViewModel
    private lateinit var petImageAdapter: PetImageAdapter

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val grantedPermissions = permissions.entries.filter { it.value }.map { it.key }
            when {
                grantedPermissions.containsAll(
                    listOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) -> {
                    Toast.makeText(
                        this,
                        "Permissions denied. Cannot upload or save images.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    Toast.makeText(
                        this,
                        "Permissions granted! You can now upload and save images.",

                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val theme = sharedPref.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(theme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // ViewModel setup
        val repository = PetRepository(NetworkModule.petApiService)
        val factory = PetViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(PetViewModel::class.java)

        // RecyclerView setup
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        petImageAdapter = PetImageAdapter(emptyList())
        recyclerView.adapter = petImageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Handle "Save Image"
        petImageAdapter.onSaveImage = { petImage ->
            if (hasRequiredPermissions()) {
                showSaveImageConfirmationDialog(petImage.url)
            } else {
                checkAndRequestPermission()
            }
        }

        // Observe pet images
        viewModel.petImages.observe(this) { images ->
            petImageAdapter.updateData(images ?: emptyList())
        }

        // "Upload Image" button logic
        val getImageFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                openImageUploadDialog(it)
            }
        }

        findViewById<Button>(R.id.addImageButton).setOnClickListener {
            checkAndRequestPermission()
            if (hasRequiredPermissions()) {
                getImageFromGallery.launch("image/*")
            }
        }

        // Search bar logic
        findViewById<EditText>(R.id.searchBar).addTextChangedListener { text ->
            val filteredList = viewModel.petImages.value?.filter {
                it.title.contains(text.toString(), ignoreCase = true) ||
                        it.description.contains(text.toString(), ignoreCase = true)
            }
            petImageAdapter.updateData(filteredList ?: emptyList())
        }

        // Sort buttons logic
        findViewById<Button>(R.id.sortAscendingButton).setOnClickListener {
            val sortedList = viewModel.petImages.value?.sortedBy { it.title }
            petImageAdapter.updateData(sortedList ?: emptyList())
        }

        findViewById<Button>(R.id.sortDescendingButton).setOnClickListener {
            val sortedList = viewModel.petImages.value?.sortedByDescending { it.title }
            petImageAdapter.updateData(sortedList ?: emptyList())
        }


        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)


    }

    // permission request logic
    private fun checkAndRequestPermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        requestPermissionLauncher.launch(permissions)
    }

    // Helper method to check permissions
    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // Getting the current theme from AppCompatDelegate
        val currentTheme = AppCompatDelegate.getDefaultNightMode()

        // Highlight the currently selected theme in the menu
        when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_NO  -> menu?.findItem(R.id.theme_light)?.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> menu?.findItem(R.id.theme_dark)?.isChecked = true
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> menu?.findItem(R.id.theme_system)?.isChecked = true
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_switch_ui -> {
                val intent = Intent(this, ComposeActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.theme_light -> {
                setAndSaveTheme(AppCompatDelegate.MODE_NIGHT_NO)
                item.isChecked = true
                true
            }
            R.id.theme_dark -> {
                setAndSaveTheme(AppCompatDelegate.MODE_NIGHT_YES)
                item.isChecked = true
                true
            }
            R.id.theme_system -> {
                setAndSaveTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                item.isChecked = true
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // React to theme changes
        when (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                setAndSaveTheme(AppCompatDelegate.MODE_NIGHT_YES)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                setAndSaveTheme(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }


    // Open dialog for image details
    private fun openImageUploadDialog(imageUri: Uri) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_image_upload, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.editTextDescription)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Edit Image Details")
            .setPositiveButton("Upload") { _, _ ->
                val title = titleInput.text.toString()
                val description = descriptionInput.text.toString()

                val newPetImage = PetImage(
                    url = imageUri.toString(),
                    title = title,
                    description = description,
                    created = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.getDefault()).format(Date())
                )
                viewModel.addPetImage(newPetImage)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    // Showing confirmation dialog for saving an image
    private fun showSaveImageConfirmationDialog(imageUrl: String) {
        AlertDialog.Builder(this)
            .setTitle("Save Image")
            .setMessage("Do you want to save this image?")
            .setPositiveButton("Yes") { dialog, _ ->
                saveImageWithProgress(imageUrl)
                dialog.dismiss()
            }
            .setNegativeButton("No", null)
            .show()
    }

    // Saving image with a progress dialog using Coroutines for async handling
    private fun saveImageWithProgress(imageUrl: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Saving image...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream: InputStream = URL(imageUrl).openStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                saveBitmapToGallery(bitmap)

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@MainActivity, "Image saved successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@MainActivity, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Saving in bitmap to gallery using scoped storage or external storage
    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val fileName = "image_${System.currentTimeMillis()}.jpg"
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = imageUri?.let { contentResolver.openOutputStream(it) }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                val image = File(imagesDir, fileName)
                outputStream = FileOutputStream(image)
            }

            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Failed to save image: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun setAndSaveTheme(themeMode: Int) {
        AppCompatDelegate.setDefaultNightMode(themeMode)


        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("theme", themeMode)
            apply()
        }


        recreate()
    }


}

