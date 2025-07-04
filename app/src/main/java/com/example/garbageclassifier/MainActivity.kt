package com.example.garbageclassifier

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.garbageclassifier.ui.theme.DarkGreen
import com.example.garbageclassifier.ui.theme.GarbageClassifierTheme
import com.example.garbageclassifier.ui.theme.LightGreen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.example.garbageclassifier.ui.theme.DarkGray
import com.example.garbageclassifier.ui.theme.Gray
import com.example.garbageclassifier.ui.theme.White


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(ClassifierViewModel::class.java)
        setContent {
            // Apply your app's theme here
            GarbageClassifierTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ImageClassifierUI(viewModel)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ImageClassifierUI(viewModel: ClassifierViewModel) {

    val context = LocalContext.current
    val prediction by viewModel.prediction.observeAsState()
    val selectedImage by viewModel.imageBitmap.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(initial = false) // Assuming you add isLoading to ViewModel

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = decodeImage(it, context)
            viewModel.selectImage(bitmap)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = DarkGreen) // A darker green for the header
                .padding(vertical = 24.dp, horizontal = 16.dp)
                .padding(top = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Garbage Classifier",
                color = White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Adds spacing between items
        ) {
            Spacer(modifier = Modifier.height(24.dp)) // Add some space after the header

            Card(
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(16.dp)), // Rounded corners for the image card
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Add shadow
                colors = CardDefaults.cardColors(containerColor = Gray) // Light gray background for card
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    selectedImage?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)), // Clip image inside card too
                            contentScale = ContentScale.Crop // Crop to fill the space
                        )
                    } ?: run {
                        // Placeholder when no image is selected
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Placeholder Image",
                                modifier = Modifier.size(80.dp),
                                tint = DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Image Selected",
                                color = DarkGray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                }
            }

            // Pick Image Button
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Make button wider but not full width
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightGreen), // Appealing green button
                shape = RoundedCornerShape(12.dp) // Rounded button corners
            ) {
                Text("Pick Image", color = White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,                fontFamily = FontFamily.Monospace)
            }

            // Predict Button
            Button(
                onClick = { viewModel.runInference() },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                enabled = selectedImage != null && !isLoading, // Enable only if image is selected and not loading
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGreen, // Darker green for predict button
                    disabledContainerColor = DarkGreen.copy(alpha = 0.5f) // Faded when disabled
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Predict", color = White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,                fontFamily = FontFamily.Monospace)
                }
            }

            // Prediction Result
            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.8f), color = LightGreen)
            } else {
                Spacer(modifier = Modifier.height(8.dp)) // Maintain consistent spacing
            }


            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(12.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Prediction Result:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkGray,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = prediction ?: "Awaiting Prediction...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightGreen ,
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.P)
fun decodeImage(uri: Uri, context: Context): Bitmap {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
        decoder.isMutableRequired = true
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
    }
}