plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinParcelize)
}

android {
    namespace = "com.example.petsgallery"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.petsgallery"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner ="androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true  // Ensure Compose is enabled
    }


}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)


    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)



    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)


    implementation(libs.androidx.navigation.compose)


    implementation(libs.firebase.firestore.ktx)


    implementation(libs.glide)

    implementation(libs.coil.compose)
    implementation(libs.coil.kt.coil.compose)


    implementation(libs.okhttp)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.core.i18n)



    testImplementation(libs.junit)
    testImplementation(libs.androidx.ui.test.junit4.android)
    testImplementation(project(":app"))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)


    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation(libs.material)
    implementation (libs.androidx.material.icons.extended)

    implementation("androidx.compose.ui:ui:1.7.2")
    implementation(libs.androidx.material)
    implementation(libs.ui.tooling.preview)
    debugImplementation("androidx.compose.ui:ui-tooling:1.3.3")
    debugImplementation(libs.ui.test.manifest)

    implementation (libs.coil)

    testImplementation (libs.junit.v4132)
    testImplementation (libs.mockito.core)
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.1.")


    testImplementation ("androidx.arch.core:core-testing:2.2.0")
    testImplementation (libs.androidx.junit.v112)
    testImplementation ("io.mockk:mockk:1.13.5")


        // Compose UI Test dependencies
        androidTestImplementation ("androidx.compose.ui:ui-test-junit4:<latest_version>")
        debugImplementation ("androidx.compose.ui:ui-test-manifest:<latest_version>")

        // AndroidX Test dependencies
        androidTestImplementation ("androidx.test.ext:junit:1.1.3")
        androidTestImplementation ("androidx.test.espresso:espresso-core:3.4.0")



}

