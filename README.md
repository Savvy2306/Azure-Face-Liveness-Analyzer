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

![image](https://github.com/user-attachments/assets/019af12e-410e-4be0-8c14-6c8b78bb6969)


## Gradle Setup

In `build.gradle (Module: app)`:

![image](https://github.com/user-attachments/assets/2e266828-0cda-4e4a-b558-5622aa937518)

## Setting Up Credentials

In your `project-level build.gradle file`:

![image](https://github.com/user-attachments/assets/66eadae2-e8e4-44f9-8278-c8fd2b88a597)

## API & Keys

In `AppConstants.kt`, add your Face API endpoint and key:

![image](https://github.com/user-attachments/assets/c5f394fd-1c8b-4eea-9100-d52d932b13c6)

## Screens
<img src="https://github.com/user-attachments/assets/ab5f5182-a7c9-4c59-98e6-c02262b5ce33" alt="image 1" width="25%" style="margin-right: 40px;"/> 
<img src="https://github.com/user-attachments/assets/f0e609b3-0fdc-407a-ba74-9e9ed8269274" alt="image 2" width="25%" />






