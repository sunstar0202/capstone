package com.example.capstone

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var classifier: TrashClassifier
    private lateinit var bluetooth: BluetoothManager

    private lateinit var scoreText: TextView
    private lateinit var pollutionGauge: ProgressBar
    private lateinit var viewFinder: PreviewView
    private lateinit var btnAnalyze: Button

    private var lastBitmap: Bitmap? = null
    private var isProcessing = false

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        classifier = TrashClassifier(this)
        bluetooth = BluetoothManager(" ") // TODO: 아두이노 맥주소 입력

        scoreText = findViewById<TextView>(R.id.scoreText)
        pollutionGauge = findViewById<ProgressBar>(R.id.pollutionGauge)
        viewFinder = findViewById<PreviewView>(R.id.viewFinder)
        btnAnalyze = findViewById<Button>(R.id.btnAnalyze)

        btnAnalyze.setOnClickListener {
            if (isProcessing) return@setOnClickListener

            val bitmap = lastBitmap
            if (bitmap == null) {
                Toast.makeText(this, "카메라 화면을 먼저 인식해야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            isProcessing = true
            processBitmap(bitmap)
        }

        Thread {
            if (bluetooth.connect()) {
                runOnUiThread {
                    Toast.makeText(this, "아두이노 연결 성공!", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (
            requestCode == CAMERA_PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer
            )

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        try {
            val bitmap = imageProxy.toBitmap()
            if (bitmap != null) {
                lastBitmap = bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            imageProxy.close()
        }
    }

    private fun processBitmap(bitmap: Bitmap) {
        val result = classifier.analyze(bitmap)

        runOnUiThread {
            scoreText.text = "${result.second.toInt()}%"
            pollutionGauge.progress = result.second.toInt()
        }

        val request = AnalysisRequest(
            label = result.first,
            score = result.second
        )

        RetrofitClient.api.sendResult(request)
            .enqueue(object : Callback<AnalysisRequest> {
                override fun onResponse(
                    call: Call<AnalysisRequest>,
                    response: Response<AnalysisRequest>
                ) {
                    if (response.isSuccessful) {
                        if (result.second <= 15.0f) {
                            bluetooth.send("O")
                        } else {
                            bluetooth.send("X")
                        }
                    } else {
                        println("서버 응답 실패: ${response.code()}")
                    }

                    isProcessing = false
                }

                override fun onFailure(call: Call<AnalysisRequest>, t: Throwable) {
                    t.printStackTrace()
                    isProcessing = false
                }
            })
    }

    private fun ImageProxy.toBitmap(): Bitmap? {
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

        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            width,
            height,
            null
        )

        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 80, out)

        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}