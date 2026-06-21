<div align="center">

# AvatarX — Real-Time Avatar & Body Tracking Android Application

<p align="center">
  <i>Bridging the physical and digital world through on-device machine learning.</i>
</p>

---

![Kotlin](https://img.shields.io/badge/Kotlin-100%%-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Native_UI-4285F4?style=for-the-badge&logo=android)
![MediaPipe](https://img.shields.io/badge/Google_MediaPipe-Pose_Tracking-0F9D58?style=for-the-badge)
![Gemini AI](https://img.shields.io/badge/Gemini_2.5_Flash-Fit_Analysis-8E75FF?style=for-the-badge&logo=googlebard&logoColor=white)

</div>

## 🌌 The Vision
**AvatarX** is not just an application; it is a "Fashion OS". It is a native Android experience designed to map human body mechanics into digital environments. By combining lightning-fast hardware-accelerated graphics with state-of-the-art AI, AvatarX extracts physical measurements, categorizes body types, and executes virtual garment try-ons without ever leaving your device.

---

## ⚡ Core Systems Architecture

### 1. 🕺 Real-Time Pose & Measurement Engine
Instead of relying on heavy frameworks like ARCore, AvatarX utilizes **Google MediaPipe**. 
* **33-Point Skeleton Tracking:** Captures complex bodily movements via live camera feed.
* **Proportion Estimation:** Mathematically extracts pixel-to-cm ratios to estimate *Shoulder Width*, *Hip Width*, and *Height*.
* **Body Type Categorization:** Automatically calculates shoulder-to-hip ratios to classify the user into `Slim`, `Regular`, `Athletic`, or `Broad`.

### 2. 🧬 The Dual-Avatar Identity
The app creates two distinct representations of the user:
* **The Visual Twin:** A high-fidelity capture of the user's physical appearance.
* **The Digital Fit Model:** An interactive, mathematical wireframe mannequin drawn natively on Canvas. Its shoulders, torso, and hips actively scale to match the user's extracted measurements.

### 3. 👕 Intelligent Virtual Try-On & Segmentation
* **ML Kit Subject Segmentation:** Scans a physical garment laid flat and magically strips away the background in milliseconds.
* **Dynamic Matrix Scaling:** Overlays garments onto the live camera feed, scaling them perfectly based on the user's live shoulder-width tracking data. 
* **Canvas Blending:** Employs advanced alpha blending and color filters so the digital clothing sits naturally on the physical body.

### 4. 🧠 Gemini AI Fashion Stylist
AvatarX sends the user's biometric data and the segmented garment to **Gemini 2.5 Flash**. The LLM processes the visual data alongside the physical measurements to return a highly personalized, stylist-level fit analysis (evaluating Comfort, Style Match, and Body Alignment).

---

## 🚀 Tech Stack Highlights

* **Language:** `Kotlin`
* **UI Framework:** `Jetpack Compose` (100% Declarative UI)
* **Graphics:** Native Android Canvas (Hardware Accelerated via Skia/Vulkan — chosen over Unity/OpenGL to keep the application lightweight and blazing fast).
* **Camera:** `CameraX API`
* **Concurrency:** `Kotlin Coroutines` & `StateFlow` (Strict Unidirectional Data Flow)

---

## 🛠️ Setup & Installation

> [!IMPORTANT]
> A physical Android device running Android 10+ (API 29+) is **highly recommended**. Emulators lack the camera performance and hardware acceleration needed for real-time ML processing.

1. **Clone the Repository**
   ```bash
   git clone https://github.com/zohaib-md/AvatarX.git
   ```

2. **Inject the Intelligence (API Key)**
   To enable the Gemini AI Fit Analysis, you must provide your API key.
   Open the `local.properties` file in the root of the project and add:
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   ```

3. **Build & Run**
   * Open the project in **Android Studio**.
   * Sync Gradle files.
   * Connect your physical Android device.
   * Hit **Run** (`Shift + F10`).

---

<div align="center">
  <p><i>Developed for the Smarrtifai Technical Assignment.</i></p>
</div>
