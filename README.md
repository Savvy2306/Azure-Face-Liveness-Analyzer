# Azure Face Liveness Analyzer

This repository demonstrates the implementation of the **Azure Face Liveness Detection** service in an Android application. It leverages Azure's AI Vision APIs to analyze faces, track liveness, and verify identity using session tokens.

## Features

- **Real-Time Face Detection & Tracking**: Detects and tracks faces across multiple image streams.
- **Liveness Detection**: Ensures the authenticity of detected faces with real-time feedback like "Look at the camera" or "Move Closer."
- **Azure Vision API Integration**: Seamlessly integrates with Azure AI Vision and Face Analysis services for comprehensive face recognition.
- **Instant User Feedback**: Provides visual cues and detailed instructions for better liveness checks.

## Technologies Used

- **Azure AI Vision API**
- **Kotlin**
- **Android SDK**
- **SurfaceView for Camera Integration**

## Version and Libraries

In `libs.version.toml`:

[versions]
azureAi = "0.17.1-beta.1"
azureAndroid = "1.0.0-beta.14"

[libraries]
azure-ai_vision_common = { group = "com.azure.ai", name = "azure-ai-vision-common", version.ref = "azureAi" }
azure-ai_vision_faceanalyzer = { group = "com.azure.ai", name = "azure-ai-vision-faceanalyzer", version.ref = "azureAi" }
azure-core-http-okhttp = { group = "com.azure.android", name = "azure-core-http-okhttp", version.ref = "azureAndroid" }

## Gradle Setup

In `build.gradle (Module: app)`:

plugins {
    id("kotlin-parcelize")
}

dependencies {
    implementation(libs.azure.core.http.okhttp)
    implementation(libs.azure.ai.vision.common)
    implementation(libs.azure.ai.vision.faceanalyzer)
}

## Setting Up Credentials

In your `project-level build.gradle file`:

repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://pkgs.dev.azure.com/msface/SDK/_packaging/AzureAIVision/maven/v1")
        name = "AzureAIVision"
        credentials {
            username = "enter your username here"
            password = "enter your password here"
        }
    }
}

## API & Keys

In `AppConstants.kt`, add your Face API endpoint and key:
const val FACE_API_ENDPOINT = "use your endpoint here"
const val FACE_API_KEY = "use face API key here"

