package com.example.capstone

import android.Manifest
import android.R.attr.label
import android.content.Intent
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
        bluetooth = BluetoothManager("DC:A6:32:88:E1:CB") // 라즈베리파이 맥주소

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
                    Toast.makeText(this, "라즈베리파이 연결 성공!", Toast.LENGTH_SHORT).show()
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
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
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
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        try {
            val bitmap = imageProxy.toBitmap()
            if (bitmap != null) { lastBitmap = bitmap }
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
                override fun onResponse(call: Call<AnalysisRequest>, response: Response<AnalysisRequest>) {

                    val serverLabel = response.body()?.label ?: "PET"
                    val serverScore = response.body()?.score ?: 0f


                    handleAnalysisResult(serverLabel, serverScore)
                }

                override fun onFailure(call: Call<AnalysisRequest>, t: Throwable) {
                    t.printStackTrace()


                    handleAnalysisResult("PET", 0f)
                }
            })
    }



    private fun handleAnalysisResult(label: String, score: Float) {

        isProcessing = false


        if (score > 15.0f) {

            bluetooth.send("X")
        } else {

            when (label) {
                "PET" -> bluetooth.send("P")
                "GLASS" -> bluetooth.send("G")
                "CAN" -> bluetooth.send("C")
                else -> bluetooth.send("X")
            }
        }

        runOnUiThread {

            showAnalysisReportDialog(label, score)
        }
    }



    private fun showAnalysisReportDialog(label: String, score: Float) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_detail)
        dialog.setCancelable(false)

        val btnConfirm = dialog.findViewById<Button>(R.id.btnClose)
        val DialogScore = dialog.findViewById<TextView>(R.id.DialogScore)
        DialogScore.text = "오염도 점수: ${score.toInt()}%"


        val title = dialog.findViewById<TextView>(R.id.title)
        val detail1 = dialog.findViewById<TextView>(R.id.detail1)
        val detail2 = dialog.findViewById<TextView>(R.id.detail2)
        val detail3 = dialog.findViewById<TextView>(R.id.detail3)
        val status1= dialog.findViewById<TextView>(R.id.Status1)
        val status2= dialog.findViewById<TextView>(R.id.Status2)
        val status3= dialog.findViewById<TextView>(R.id.Status3)

        if (score <= 5.0f) {
            status1?.text = "완료 (통과)"
            status1?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))

            status2?.text = "깨끗함"
            status2?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))

            status3?.text = "양호"
            status3?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))

        } else if (score <= 15.0f) {

            status1?.text = "미흡 (주의)"
            status1?.setTextColor(android.graphics.Color.parseColor("#FF9800"))

            status2?.text = "깨끗함"
            status2?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))

            status3?.text = "양호"
            status3?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        } else {

            status1?.text = "미흡 (감점)"
            status1?.setTextColor(android.graphics.Color.parseColor("#FF3B30"))

            status2?.text = "오염 검출"
            status2?.setTextColor(android.graphics.Color.parseColor("#FF3B30"))

            status3?.text = "불량"
            status3?.setTextColor(android.graphics.Color.parseColor("#FF3B30"))
        }

        when(label) {
            "CAN" -> {
                title?.text = "📊 CAN 정밀 분석 리포트"
                detail1?.text = "🔩 이물질 투입 여부"
                detail2?.text = "🥫 압착/찌그러짐 상태"
                detail3?.text = "🛡️ 캔 내부 세척도"
            }
            "GLASS" -> {
                title?.text = "📊 GLASS 정밀 분석 리포트"
                detail1?.text = "🍶 파손/크랙 여부"
                detail2?.text = "🪙 병뚜껑 분리 여부"
                detail3?.text = "🧼 유리 변색 상태"
            }
            else -> {
                title?.text = "📊 PET 정밀 분석 리포트"
                detail1?.text = "🏷️ 비닐 라벨 제거"
                detail2?.text = "💧 내부 잔여물/액체"
                detail3?.text = "🔍 재질 투명도"
            }
        }


        btnConfirm.setOnClickListener {
            dialog.dismiss()

            val intent = Intent(this, ResultActivity::class.java)
            startActivity(intent)
            finish()
        }

        dialog.show()
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
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 80, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}