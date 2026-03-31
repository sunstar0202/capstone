package com.example.capstone

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class TrashClassifier(context: Context) {
    private var interpreter: Interpreter? = null

    init {
        try {
            val assetFileDescriptor = context.assets.openFd("model.tflite")
            val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, assetFileDescriptor.startOffset, assetFileDescriptor.declaredLength)
            interpreter = Interpreter(modelBuffer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun analyze(bitmap: Bitmap): Pair<String, Float> {

        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val input = convertBitmapToBuffer(resized)
        val output = Array(1) { FloatArray(2) } // [0]:Clean, [1]:Dirty


        interpreter?.run(input, output)

        val score = output[0][1] * 100 // 오염 확률 %
        val label = if (output[0][0] > output[0][1]) "Clean" else "Dirty"

        return Pair(label, score)
    }

    private fun convertBitmapToBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3).apply { order(ByteOrder.nativeOrder()) }
        val pixels = IntArray(224 * 224)
        bitmap.getPixels(pixels, 0, 224, 0, 0, 224, 224)
        for (pixel in pixels) {
            buffer.putFloat(((pixel shr 16 and 0xFF) / 255f))
            buffer.putFloat(((pixel shr 8 and 0xFF) / 255f))
            buffer.putFloat(((pixel and 0xFF) / 255f))
        }
        return buffer
    }
}