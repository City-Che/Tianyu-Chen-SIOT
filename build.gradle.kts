plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // 添加 Google 服务插件
    id("org.jetbrains.kotlin.android") // 启用 Kotlin 插件
}

android {
    namespace = "com.example.siot" // 替换为你的包名
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.siot"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // 使用 Compose Compiler 1.5.3
    }
}

dependencies {
    // AndroidX AppCompat 支持库
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.2.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")

    // MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.0.0")

    // Compose BOM (确保版本号与 Compose UI 的兼容性一致)
    implementation(platform("androidx.compose:compose-bom:2023.09.01"))

    // Compose Core UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Compose Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.1")

    // Debugging Tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.2")

    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

