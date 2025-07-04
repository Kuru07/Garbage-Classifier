package com.example.garbageclassifier

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import android.graphics.Color

class ClassifierViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private lateinit var tflite: Interpreter

    private val _prediction = MutableLiveData<String>()
    val prediction: LiveData<String> = _prediction

    private val _imageBitmap = MutableLiveData<Bitmap?>()
    val imageBitmap: LiveData<Bitmap?> = _imageBitmap

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        val model = FileUtil.loadMappedFile(appContext, "quantized_model.tflite")
        tflite = Interpreter(model)
    }

    fun selectImage(bitmap: Bitmap) {
        _imageBitmap.postValue(bitmap)
    }

    fun runInference() {
        val bitmap = _imageBitmap.value ?: return

        _isLoading.postValue(true)

        val input = preprocessImage(bitmap)
        val output = Array(1) { FloatArray(12) }
        tflite.run(input, output)

        val classLabels = listOf(
            "Battery",
            "Biological",
            "Brown Glass",
            "Cardboard",
            "Clothes",
            "Green Glass",
            "Metal",
            "Paper",
            "Plastic",
            "Shoes",
            "Trash",
            "White Glass"
        )

        val predictedIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        val confidence = output[0][predictedIndex]  // Between 0 and 1

        val predictedLabel = classLabels.getOrNull(predictedIndex) ?: "Unknown"

        _prediction.postValue("Prediction: $predictedLabel\nConfidence: ${"%.2f".format(confidence * 100)}%")
        _isLoading.postValue(false)    }

    // MODIFIED FUNCTION: Remove explicit -1 to 1 normalization
    private fun preprocessImage(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val resizedImage = Bitmap.createScaledBitmap(bitmap, 300, 300, true)
        val input = Array(1) { Array(300) { Array(300) { FloatArray(3) } } }

        for (y in 0 until 300) {
            for (x in 0 until 300) {
                val pixel = resizedImage.getPixel(x, y)
                // Assuming the TFLite model now handles the -1 to 1 normalization internally
                // We just need to convert 0-255 values to Float and assign to the array
                input[0][y][x][0] = Color.red(pixel).toFloat()
                input[0][y][x][1] = Color.green(pixel).toFloat()
                input[0][y][x][2] = Color.blue(pixel).toFloat()
            }
        }
        return input
    }
}