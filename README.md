# PetVitals

PetVitals is an Android application built to help pet owners track their pet's health and vital information.

## Technologies Used

*   **UI:** Jetpack Compose
*   **Dependency Injection:** Hilt
*   **Backend:** Firebase (Authentication, Firestore)
*   **Navigation:** Jetpack Navigation Compose
*   **Asynchronous Programming:** Kotlin Coroutines
*   **Image Loading:** Coil

## Architecture

The app follows a modern Android architecture pattern:

*   **UI Layer:** Built with Jetpack Compose, the UI layer is responsible for displaying the application's data and handling user interactions.
*   **ViewModel Layer:** The ViewModels are responsible for holding and managing UI-related data in a lifecycle-conscious way. They expose data to the UI and handle user logic.
*   **Data Layer:** The data layer is responsible for fetching and storing data from various sources, such as a remote server or a local database. In this case, Firebase Firestore is used as the remote data source.

## Features

*   **User Authentication:** Sign up, log in, and reset password functionality.
*   **Pet Management:** Add new pets, edit existing pet details, and view a list of all your pets.
*   **Pet Profile:** A detailed view of each pet, including their information, medications, and food.
*   **Records:** Keep track of your pet's records for various situations.
*   **Food and Medication Tracking:** Log and manage your pet's food and medication intake.
*   **Share Pet Profiles:** Share a pet's profile with other users.
*   **User Profile:** View and manage your user profile.

## Getting Started

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/ETO-YTKA/petvitals.git
    ```
2.  **Set up Firebase:**
    *   Create a new Firebase project.
    *   Add an Android app to your Firebase project with the package name `com.example.petvitals`.
    *   Download the `google-services.json` file and place it in the `app` directory.
3.  **Build and run the app:**
    *   Open the project in Android Studio.
    *   Sync the project with Gradle files.
    *   Run the app on an emulator or a physical device.
