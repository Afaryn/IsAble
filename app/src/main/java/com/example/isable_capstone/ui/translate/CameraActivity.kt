package com.example.isable_capstone.ui.translate

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.isable_capstone.R
import com.example.isable_capstone.databinding.ActivityCameraBinding
import com.example.isable_capstone.ml.SignLanguageModelV1
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imageCapture: ImageCapture? = null
    private lateinit var interpreter: Interpreter
    private val executor = Executors.newSingleThreadExecutor()
    lateinit var model:SignLanguageModelV1


    private fun loadModelFile(modelPath: String): MappedByteBuffer {
        val assetFileDescriptor = assets.openFd(modelPath)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        interpreter = Interpreter(loadModelFile("sign_language_model_v1.tflite"))
//        model = SignLanguageModelV1.newInstance(this)
        setupView()

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        binding.switchCamera.setOnClickListener {
            cameraSelector =
                if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
                else CameraSelector.DEFAULT_BACK_CAMERA

        }

        binding.captureImage.setOnClickListener {
            captureImage()
        }

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(
                    this@CameraActivity,
                    "Failed to start camera.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "startCamera: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImage() {
        imageCapture?.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    processImage(image)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Toast.makeText(
                        this@CameraActivity,
                        "Error capturing image: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun processImage(image: ImageProxy) {
        val bitmap = imageToBitmap(image)
        image.close()

        // Run inference on the captured image
        val result = runInference(bitmap)

        // Process the result as needed (e.g., display in a fragment or activity)
        // ...

        // For example, displaying the result in a Toast
        Toast.makeText(this, "Inference Result: $result", Toast.LENGTH_SHORT).show()
    }

    private fun imageToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val bitmap = Bitmap.createBitmap(
            image.width, image.height, Bitmap.Config.ARGB_8888
        )

        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }

    private fun runInference(bitmap: Bitmap): String {
        // Preprocess the image before running inference
        val inputTensor = preprocessInput(bitmap)

        // Run inference
        val outputs = TensorBuffer.createFixedSize(intArrayOf(1, 26), DataType.FLOAT32)
        interpreter.run(inputTensor.buffer, outputs.buffer)

        // Postprocess the output
//        val resultIndex = outputs.floatArray.indices.maxByOrNull { outputs } ?: -1
        val resultIndex = argmax(outputs.floatArray)
        val labels = Array(26) { "class${it + 1}" } // Adjust based on your model labels

        return labels[resultIndex]
    }

    private fun argmax(array: FloatArray): Int {
        var maxIndex = 0
        var maxValue = array[0]

        for (i in 1 until array.size) {
            if (array[i] > maxValue) {
                maxIndex = i
                maxValue = array[i]
            }
        }

        return maxIndex
    }

    private fun preprocessInput(bitmap: Bitmap): TensorBuffer {
        val modelInputSize = 244 // Adjust based on your model input size
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize * modelInputSize * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        // Resize the bitmap to match the model input size
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, modelInputSize, modelInputSize, true)

        // Normalize pixel values to the range of [0, 1]
        val intValues = IntArray(modelInputSize * modelInputSize)
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)

        for (value in intValues) {
            byteBuffer.putFloat((value and 0xFF) / 255.0f)
        }

        byteBuffer.rewind()

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 244, 244, 1), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        return inputFeature0
    }

//    private fun postprocessOutput(outputTensor: Array<FloatArray>): String {
//        // Postprocess the output tensor as needed
//        // ...
//
//        // For example, find the index of the maximum value
//        val resultIndex = outputTensor[0].indices.maxByOrNull { outputTensor[0][it] } ?: -1
//        val labels = Array(26) { "class${it + 1}" } // Adjust based on your model labels
//        return labels[resultIndex]
//    }

    override fun onDestroy() {
        // Clean up resources
        model.close()
        executor.shutdown()
        super.onDestroy()
    }


    private fun setupView(){
        val toolbar = findViewById<Toolbar>(R.id.xml_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title="Translate"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Tindakan yang akan diambil saat tombol back ditekan
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
        private const val TAG = "CameraActivity"
        const val EXTRA_CAMERAX_IMAGE = "CameraX Image"
        const val CAMERAX_RESULT = 200
    }
}