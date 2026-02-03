plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

}

android {
    namespace = "com.example.playlistmaker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.playlistmaker"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
// Glide
    // Glide
    implementation("com.github.bumptech.glide:glide:4.15.1") {
        exclude(group = "com.android.support")
    }

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Activity and Fragment
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Material Design
    implementation(libs.material)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // RecyclerView and CardView
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)

    // AppCompat and ConstraintLayout
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)

    // Gson
    implementation(libs.gson)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    // Compose dependencies через BOM
    implementation(libs.ui)
    implementation(libs.androidx.compose.ui.ui.tooling)
    implementation(libs.ui.graphics)
    implementation(libs.material3)
    implementation(libs.androidx.material3.icons)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug dependencies
    debugImplementation(libs.androidx.compose.ui.ui.tooling2)
    debugImplementation(libs.ui.test.manifest)
}

