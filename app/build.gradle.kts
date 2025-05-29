plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packaging {
        resources {
            // Exclude conflicting or unnecessary resources
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/NOTICE",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE.txt",
                "META-INF/NOTICE.md"
            )
        }
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.google.android.gms:play-services-location:21.0.1") // For location services
    implementation("com.google.firebase:firebase-database:20.0.5") // For Firebase Realtime Database
    implementation("com.google.firebase:firebase-core:21.0.0") // Optional: for Firebase Analytics
    implementation("com.twilio.sdk:twilio:10.6.1") // Ensure the correct Twilio SDK version

    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.support.annotations)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
