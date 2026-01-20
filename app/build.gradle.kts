plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.dagger.hilt.android)
    alias(libs.plugins.kotlin.kapt)
    id("kotlin-parcelize")
    id("jacoco")
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("com.google.gms.google-services")
}
jacoco {
    toolVersion = "0.8.11"
}

android {
    namespace = "com.dishut_lampung.sitanihut"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dishut_lampung.sitanihut"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.dishut_lampung.sitanihut.CustomTestRunner"
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            buildConfigField("String", "BASE_URL", "\"https://api-sipetahut.palum.id/api/\"")
        }

        release {
            buildConfigField ("String", "BASE_URL", "\"https://sitanihut.lampungprov.go.id/backend/api/\"")
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
    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/NOTICE",
                "/META-INF/{AL2.0,LGPL2.1}"
            )
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
//    packaging {
//        resources {
//            excludes += "/META-INF/{AL2.0,LGPL2.1}"
//        }
//    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
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
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-text-google-fonts")
    implementation(libs.core.ktx)
//    implementation(libs.androidx.room.compiler.processing.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("com.google.truth:truth:1.1.3")
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // turbine
    testImplementation("app.cash.turbine:turbine:1.0.0")

    // splash screen broohh
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    androidTestImplementation ("com.google.dagger:hilt-android-testing:2.48")
    kaptAndroidTest ("com.google.dagger:hilt-android-compiler:2.48")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")

    // Room
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    // Datastore
    implementation ("androidx.datastore:datastore-preferences:1.1.1")

    // Coil
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp & Logging Interceptor
    testImplementation ("com.squareup.okhttp3:mockwebserver:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")

    // MockK
    testImplementation("io.mockk:mockk:1.13.10")
    androidTestImplementation("io.mockk:mockk-android:1.13.10")

    // robolectric: test room
    testImplementation("org.robolectric:robolectric:4.14.1")

    // paging 3
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")
    implementation("androidx.room:room-paging:2.6.1")

    // work manager
    implementation(libs.androidx.hilt.common)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
    testImplementation("androidx.work:work-testing")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // firebase cloud messaging
    implementation(libs.firebase.messaging)
}


detekt {
    toolVersion = "1.23.6"
    config.setFrom(files("$rootDir/config/detekt/detekt.yml")) // point to your custom config defining rules to run, overwriting default behavior
    baseline =
        file("$rootDir/config/baseline.xml") // a way of suppressing issues before introducing detekt
    ignoreFailures = true
    buildUponDefaultConfig = true
    allRules = false // activate all available (even unstable) rules.
}

val exclusions = listOf(
    "jdk.internal.**",
    "kotlin.*",
    "org.robolectric.*",
    "com.google.*",
    "androidx.*",
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "android/**/*.*",

    // Hilt & Dagger (DI)
    "**/di/**",
    "**/*_HiltModules*",
    "**/hilt_aggregated_deps/**",
    "**/*_Factory*",
    "**/*_MembersInjector*",
    "**/dagger/**",

    // Android UI (Activity, Fragment, Adapter, Application)
    "**/*Activity*",
    "**/*Fragment*",
    "**/*Adapter*",
    "**/*Application*",

    // Jetpack Compose UI (Screen, Components, Generated)
    "**/presentation/**/components/**",
    "**/presentation/**/ui/**",
    "**/*Screen*",
    "**/*Kt*",
    "**/*\$Composable*",
    "**/*Preview*",

    // Data Classes (Model, DTO, Entity, Response)
    "**/model/**",
    "**/dto/**",
    "**/response/**",
    "**/entity/**",

    // Generated Code (Room, Retrofit)
    "**/*_Impl*"
)

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    group = "Reporting"
    description = "Generate JaCoCo coverage reports excluding UI and DI."

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(exclusions)
    }

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(layout.buildDirectory.get()) {
        include(listOf("**/*.exec", "**/*.ec"))
    })
}

tasks.withType<Test>().configureEach {
    jvmArgs(
        "-noverify",
        "-Xmx3g",
        "-XX:+UseG1GC",
        "-XX:MaxMetaspaceSize=1g",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/java.net=ALL-UNNAMED"
    )
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        includes = listOf("com.dishut_lampung.sitanihut.*")
    }
}