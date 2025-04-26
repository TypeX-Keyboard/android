plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.service)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.app.distribution)
}

android {
    namespace = "cc.typex.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "cc.typex.app"
        minSdk = 29
        targetSdk = 35
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.fragment.ktx)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.rxkotlin)

    implementation(libs.autodispose)
    implementation(libs.autodispose.lifecycle)
    implementation(libs.autodispose.android)
    implementation(libs.autodispose.androidx.lifecycle)


    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.rxjava3)
    implementation(libs.androidx.room.paging)

    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.rxjava3)

    implementation(libs.gson)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.adapter.rxjava3)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.timber)

    implementation(libs.androidx.security.crypto)

    implementation(libs.trust.wallet.core)

    implementation(libs.lottie)

    // RefreshLayout
    implementation(libs.refresh.layout.kernel)
    implementation(libs.refresh.layout.header.classics)
    implementation(libs.refresh.layout.footer.classics)

    // Glide
    implementation(libs.glide)
    kapt(libs.glide.compiler)
    implementation(libs.glide.okhttp3.integration)

    implementation(libs.androidx.browser)
    implementation(libs.androidx.biometric)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
}