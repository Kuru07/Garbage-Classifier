# 📱 Garbage Classifier Android App using TensorFlow Lite

An Android application that classifies garbage images into **12 distinct categories** using a TensorFlow Lite model.  
The app is built using **Jetpack Compose** and integrates on-device machine learning for real-time predictions.

---

## 🚀 Table of Contents

- [About the Project](#about-the-project)
- [Tech Stack](#tech-stack)
- [Model Details](#model-details)
- [App Architecture](#app-architecture)
- [Setup and Installation](#setup-and-installation)
- [Key Files Explained](#key-files-explained)
- [How It Works](#how-it-works)
- [Screenshots](#screenshots)
- [APK Download](#apk-download)
- [Future Improvements](#future-improvements)
- [Acknowledgments](#acknowledgments)

---

## 📚 About the Project

There is a growing need for **intelligent waste management systems** to promote recycling and proper disposal.  
This app helps classify garbage into:

- Battery
- Biological
- Brown Glass
- Cardboard
- Clothes
- Green Glass
- Metal
- Paper
- Plastic
- Shoes
- Trash
- White Glass

By simply selecting an image from your gallery, the app predicts the garbage category along with the confidence score.

---

## 🛠️ Tech Stack

- **Android Studio Giraffe**
- **Jetpack Compose**
- **TensorFlow Lite (TFLite)**
- **MVVM Architecture**
- **LiveData & ViewModel**
- **Kotlin**

---

## 🧐 Model Details

- **Model Type:** TensorFlow Lite Quantized Model
- **Input Size:** 300x300 RGB image
- **Classes:** 12 garbage categories
- **Model File Location:**

```text
app/src/main/assets/quantized_model.tflite
```

---

## 🏗️ App Architecture

- **MVVM Pattern**
- `MainActivity`: UI layer (Jetpack Compose)
- `ClassifierViewModel`: Handles image selection, preprocessing, and running inference.
- `quantized_model.tflite`: TensorFlow Lite model stored in the assets folder.

---

## ⚙️ Setup and Installation

### 1. Clone the Repository

```bash
git clone https://github.com/Kuru07/Garbage-Classifier.git
```

### 2. Open in Android Studio

### 3. Add TensorFlow Lite Dependencies

These are already configured in your `build.gradle`:

```gradle
implementation(libs.tensorflow.lite.support)
implementation(libs.tensorflow.lite)
implementation(libs.tensorflow.lite.gpu)
```

### 4. File Structure

```text
app/
 └─ src/
      └─ main/
           ├─ assets/
           │    └─ quantized_model.tflite
           ├─ java/com/example/garbageclassifier/
           │    ├─ MainActivity.kt
           │    └─ ClassifierViewModel.kt
           ├─ res/
           └─ AndroidManifest.xml
```

---

## 🔍 Key Files Explained

### `build.gradle` (app module)

- Adds required TensorFlow Lite dependencies.
- Enables code shrinking and resource optimization in release builds.

```gradle
implementation(libs.tensorflow.lite.support)
implementation(libs.tensorflow.lite)
implementation(libs.tensorflow.lite.gpu)
```

This block ensures that TensorFlow Lite libraries are available for running the model.

---

### `ClassifierViewModel.kt`

This ViewModel handles loading the TensorFlow Lite model and performing the inference.

```kotlin
val model = FileUtil.loadMappedFile(appContext, "quantized_model.tflite")
tflite = Interpreter(model)
```

This code loads the TensorFlow Lite model from the assets folder.

```kotlin
val output = Array(1) { FloatArray(12) }
tflite.run(input, output)
```

The TensorFlow Lite interpreter runs the model using the input image and produces the prediction in the `output` array.

```kotlin
val predictedLabel = classLabels.getOrNull(predictedIndex) ?: "Unknown"
_prediction.postValue("Prediction: $predictedLabel\nConfidence: ${"%.2f".format(confidence * 100)}%")
```

This block maps the predicted index to its corresponding label and updates the LiveData.

---

### `MainActivity.kt`

This file defines the user interface and connects the UI to the ViewModel.

```kotlin
val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri: Uri? ->
    uri?.let {
        val bitmap = decodeImage(it, context)
        viewModel.selectImage(bitmap)
    }
}
```

This block allows the user to pick an image from their gallery.

```kotlin
Button(
    onClick = { viewModel.runInference() },
    enabled = selectedImage != null && !isLoading,
)
```

The predict button triggers the inference only when an image is selected and the app is not loading.

---

### `quantized_model.tflite`

The pre-trained garbage classification model that is used for making predictions. This file must be placed in the `assets` folder.

---

## ⚡ How It Works

1. **Select Image:**  
   Click the "Pick Image" button to select an image from your gallery.

2. **Run Inference:**  
   Click "Predict" to classify the selected image.

3. **Get Results:**  
   The predicted garbage type and its confidence score are displayed instantly.

4. **Image Preprocessing:**  
   The image is resized to `300x300` and RGB pixel values are passed to the TensorFlow Lite model.

---

## 📸 Screenshots

| Home Screen | Prediction Screen |
| ----------- | ----------------- |
| ![Home](https://i.ibb.co/Jw4QVkcn/Screenshot-20250704-160450.jpg) | ![Predict](https://i.ibb.co/0j8BwtV3/Screenshot-20250704-160459.jpg) |


---

## 👅 APK Download

[**Download APK here**](https://drive.google.com/drive/folders/1GklhDnnUoHpD0U5y4US-HeP4UU-461lc)  


---

## 🚀 Future Improvements

- Add Camera support for real-time image capture.
- Support multi-label classification for mixed garbage.
- Offline model updates.
- Detailed recycling guidelines for each class.

---

## 🙏 Acknowledgments

- TensorFlow Lite team for mobile inference libraries.
- Kaggle for the garbage classification dataset.
- Android Compose community for UI inspirations.
