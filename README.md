# AvatarX - Intelligent Fashion OS 🚀

AvatarX is a next-generation Android application that bridges the gap between digital identity and intelligent garment fitting. Built with a premium "Fashion OS" aesthetic, the app leverages on-device Machine Learning (MediaPipe & ML Kit) and Generative AI (Gemini) to create an unparalleled virtual try-on experience.

## ✨ Core Features

* **Real-time Body Scanning:** Uses CameraX and Google MediaPipe to track 33 full-body skeletal landmarks in real-time.
* **Dual Avatar System:** Generates both a "Visual Twin" (photographic) and a "Digital Fit Model" (an interactive wireframe mannequin scaled to exact user measurements).
* **Smart Measurement Extraction:** Calculates shoulder width, hip width, and height to automatically categorize body types (Slim, Regular, Athletic, Broad).
* **Garment Segmentation:** Utilizes Google ML Kit Subject Segmentation to instantly remove backgrounds from scanned clothing items.
* **Dynamic Virtual Try-On:** Scales and overlays garments onto the user's live body tracking data using custom Native Canvas Matrix transformations.
* **Gemini AI Fit Analysis:** Sends user measurements, the body image, and the garment image to `gemini-1.5-pro` to generate personalized, stylist-level fit insights.

## 🛠️ Technology Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (100% Declarative UI)
* **Graphics:** Native Android Canvas (Hardware Accelerated via Skia/Vulkan)
* **Camera:** CameraX API
* **Machine Learning:** 
  * Google MediaPipe (Pose Detection)
  * Google ML Kit (Subject Segmentation)
* **Generative AI:** Google Generative AI SDK (Gemini 1.5 Pro)
* **Concurrency:** Kotlin Coroutines & Flow

## ⚙️ Setup Instructions

### Prerequisites
* Android Studio (Koala or newer recommended)
* A physical Android device running Android 10+ (API 29+) is **highly recommended** for optimal camera and ML performance. Emulators may struggle with live camera feeds and ML processing.
* Gemini API Key

### Installation

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/yourusername/AvatarX.git
   ```

2. **Add Gemini API Key:**
   To enable the AI Fit Analysis, you must provide a valid Gemini API key.
   * Open the `local.properties` file in the root of the project.
   * Add the following line:
     ```properties
     GEMINI_API_KEY=your_actual_api_key_here
     ```

3. **Build and Run:**
   * Open the project in Android Studio.
   * Sync Project with Gradle Files.
   * Connect your Android device via USB debugging.
   * Click **Run** (`Shift + F10`).

## 📐 Architecture Highlights

* **No Heavy 3D Engines:** Opted against Unity/OpenGL in favor of Jetpack Compose's native hardware-accelerated Canvas. This kept the APK size small and performance blazing fast while still achieving complex 2D/3D visual overlays.
* **State Management:** Fully reactive Unidirectional Data Flow (UDF) using `StateFlow` and Hilt ViewModels.
* **Seamless Transitions:** Utilized `AnimatedContent` and `AnimatedVisibility` for buttery smooth glassmorphism UI transitions.

## 📱 Screenshots & Demo
*(Add screenshots or a GIF here before submitting)*

---
*Developed for the Smarrtifai Technical Assignment.*
