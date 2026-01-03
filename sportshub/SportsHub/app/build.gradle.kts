plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    id("com.google.gms.google-services")
    alias(libs.plugins.navigation.safeargs.kotlin)
}

android {
    namespace = "com.example.sportshub"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.sportshub"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Fragment & Activity
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation(libs.androidx.activity)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Glide pro obrázky
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
}
