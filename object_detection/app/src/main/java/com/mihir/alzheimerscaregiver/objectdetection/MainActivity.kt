package com.mihir.alzheimerscaregiver.objectdetection

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.mihir.alzheimerscaregiver.objectdetection.Constants.LABELS_PATH
import com.mihir.alzheimerscaregiver.objectdetection.Constants.MODEL_PATH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), Detector.DetectorListener {
    private val isFrontCamera = false

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private var detector: Detector? = null

    private lateinit var cameraExecutor: ExecutorService
    
    private lateinit var viewFinder: PreviewView
    private lateinit var overlay: OverlayView
    private lateinit var inferenceTime: TextView
    private lateinit var isGpu: ToggleButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Debug: Log which layout we're trying to load
        Log.d("MainActivity", "Loading layout...")
        
        try {
            // First, let's try to create a simple programmatic layout to test
            val parentLayout = androidx.constraintlayout.widget.ConstraintLayout(this)
            parentLayout.id = android.view.View.generateViewId()
            parentLayout.layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            parentLayout.setBackgroundColor(android.graphics.Color.BLACK)
            
            // Create PreviewView programmatically
            viewFinder = androidx.camera.view.PreviewView(this)
            viewFinder.id = android.view.View.generateViewId()
            val previewParams = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT,
                0
            )
            previewParams.dimensionRatio = "3:4"
            previewParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            previewParams.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            previewParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            previewParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            viewFinder.layoutParams = previewParams
            
            // Create OverlayView programmatically
            overlay = OverlayView(this, null)
            overlay.id = android.view.View.generateViewId()
            val overlayParams = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                0,
                0
            )
            overlayParams.dimensionRatio = "3:4"
            overlayParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            overlayParams.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            overlayParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            overlayParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            overlay.layoutParams = overlayParams
            overlay.translationZ = 5f
            
            // Create TextView for inference time
            inferenceTime = android.widget.TextView(this)
            inferenceTime.id = android.view.View.generateViewId()
            inferenceTime.text = "0ms"
            inferenceTime.setTextColor(android.graphics.Color.WHITE)
            val textParams = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT,
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            textParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            textParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            textParams.setMargins(32, 32, 32, 32)
            inferenceTime.layoutParams = textParams
            
            // Create ToggleButton for GPU
            isGpu = android.widget.ToggleButton(this)
            isGpu.id = android.view.View.generateViewId()
            isGpu.textOff = "GPU"
            isGpu.textOn = "GPU"
            isGpu.setBackgroundColor(android.graphics.Color.parseColor("#FF9800"))
            isGpu.setTextColor(android.graphics.Color.WHITE)
            val buttonParams = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT,
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            buttonParams.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            buttonParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            buttonParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            buttonParams.setMargins(0, 0, 0, 32)
            isGpu.layoutParams = buttonParams
            
            // Add all views to parent
            parentLayout.addView(viewFinder)
            parentLayout.addView(overlay)
            parentLayout.addView(inferenceTime)
            parentLayout.addView(isGpu)
            
            setContentView(parentLayout)
            Log.d("MainActivity", "Programmatic layout created successfully")
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to create layout", e)
            throw e
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Add uncaught exception handler for the activity
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e("MainActivity", "Uncaught exception in thread ${thread.name}", exception)
            // Let the default handler also run
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            defaultHandler?.uncaughtException(thread, exception)
        }

        cameraExecutor.execute {
            try {
                Log.d("MainActivity", "Initializing detector...")
                detector = Detector(baseContext, MODEL_PATH, LABELS_PATH, this) {
                    Log.d("MainActivity", "Detector message: $it")
                    toast(it)
                }
                Log.d("MainActivity", "Detector initialized successfully")
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to initialize detector", e)
                runOnUiThread {
                    toast("Failed to initialize object detection: ${e.message}")
                }
            }
        }

        Log.d("MainActivity", "Checking permissions...")
        if (allPermissionsGranted()) {
            Log.d("MainActivity", "Permissions granted, starting camera")
            startCamera()
        } else {
            Log.d("MainActivity", "Requesting permissions")
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        isGpu.setOnCheckedChangeListener { _, isChecked ->
            cameraExecutor.execute {
                detector?.restart(isGpu = isChecked)
            }
        }
    }

    private fun startCamera() {
        Log.d("MainActivity", "Starting camera...")
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            cameraProviderFuture.addListener({
                try {
                    Log.d("MainActivity", "Camera provider ready")
                    cameraProvider = cameraProviderFuture.get()

                    preview = Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build()
                        .also {
                            it.setSurfaceProvider(viewFinder.surfaceProvider)
                        }

                    imageAnalyzer = ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor) { imageProxy ->
                                try {
                                    // Add safety checks
                                    if (imageProxy.image == null) {
                                        Log.w("MainActivity", "ImageProxy has null image")
                                        return@setAnalyzer
                                    }
                                    
                                    Log.d("MainActivity", "Processing image: ${imageProxy.width}x${imageProxy.height}, format: ${imageProxy.format}")
                                    
                                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                                    val bitmap = imageProxyToBitmap(imageProxy)
                                    
                                    // Check if bitmap is valid
                                    if (bitmap.isRecycled) {
                                        Log.w("MainActivity", "Bitmap is recycled, skipping detection")
                                        return@setAnalyzer
                                    }
                                    
                                    val rotatedBitmap = rotateBitmap(bitmap, rotationDegrees.toFloat())
                                    
                                    // Only run detection if detector is available
                                    detector?.let { det ->
                                        det.detect(rotatedBitmap)
                                    } ?: Log.w("MainActivity", "Detector not available, skipping detection")
                                    
                                    // Clean up bitmaps to prevent memory leaks
                                    if (bitmap != rotatedBitmap && !bitmap.isRecycled) {
                                        bitmap.recycle()
                                    }
                                    
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Error in image analysis", e)
                                } finally {
                                    imageProxy.close()
                                }
                            }
                        }

                    val cameraSelector = if (isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA
            else CameraSelector.DEFAULT_BACK_CAMERA

            try {
                Log.d("MainActivity", "Binding camera use cases...")
                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
                Log.d("MainActivity", "Camera bound successfully")
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                runOnUiThread {
                    toast("Camera binding failed: ${exc.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up camera", e)
            runOnUiThread {
                toast("Camera setup failed: ${e.message}")
            }
        }
        }, ContextCompat.getMainExecutor(this))
    } catch (e: Exception) {
        Log.e("MainActivity", "Failed to get camera provider", e)
        toast("Failed to initialize camera: ${e.message}")
    }
    }

    private fun imageProxyToBitmap(imageProxy: androidx.camera.core.ImageProxy): Bitmap {
        return try {
            // Use a safer approach that handles different image formats
            val image = imageProxy.image ?: throw IllegalArgumentException("Image is null")
            
            when (image.format) {
                android.graphics.ImageFormat.YUV_420_888 -> {
                    // Most common format - handle safely
                    convertYuv420888ToBitmap(image)
                }
                android.graphics.ImageFormat.NV21 -> {
                    // Legacy format - handle safely
                    convertNv21ToBitmap(image)
                }
                else -> {
                    // Fallback: create a simple bitmap
                    Log.w("MainActivity", "Unsupported image format: ${image.format}, creating placeholder")
                    Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error converting ImageProxy to Bitmap", e)
            // Return a small placeholder bitmap to avoid crashes
            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        }
    }
    
    private fun convertYuv420888ToBitmap(image: android.media.Image): Bitmap {
        val planes = image.planes
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, image.width, image.height, null)
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, image.width, image.height), 75, out)
        val imageBytes = out.toByteArray()
        return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
    
    private fun convertNv21ToBitmap(image: android.media.Image): Bitmap {
        val planes = image.planes
        val yBuffer = planes[0].buffer
        val vuBuffer = planes[1].buffer

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, image.width, image.height, null)
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, image.width, image.height), 75, out)
        val imageBytes = out.toByteArray()
        return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        runOnUiThread {
            this.inferenceTime.text = "${inferenceTime}ms"
            overlay.setResults(boundingBoxes)
            overlay.invalidate()
        }
    }

    override fun onEmptyDetect() {
        runOnUiThread {
            overlay.setResults(listOf())
            overlay.invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy called")
        cameraExecutor.shutdown()
        detector?.close()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("MainActivity", "Permission result: requestCode=$requestCode, results=${grantResults.contentToString()}")
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                Log.d("MainActivity", "Permissions granted, starting camera from permission result")
                startCamera()
            } else {
                Log.e("MainActivity", "Permissions not granted by user")
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun toast(message: String) {
        runOnUiThread {
            Toast.makeText(baseContext, message, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val TAG = "Camera"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(Manifest.permission.CAMERA).toTypedArray()
    }
}
