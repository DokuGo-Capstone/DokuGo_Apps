// In `PredictionHelper.kt`
package com.dokugo.prediction

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PredictionHelper(context: Context) {

    private val interpreter: Interpreter

    init {
        interpreter = Interpreter(loadModel(context))
    }

    private fun loadModel(context: Context): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd("model_with_select_ops.tflite")
        val fileInputStream = assetFileDescriptor.createInputStream()
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun predict(expenses: List<Float>): Float {
        val normalizedAmount = expenses.last() / 187.85f
        val logAmount = Math.log1p(normalizedAmount.toDouble()).toFloat()
        val inputFeatures = floatArrayOf(
            expenses.getOrNull(expenses.size - 2) ?: 0f, // Lag_1_Expenses
            expenses.getOrNull(expenses.size - 3) ?: 0f, // Lag_2_Expenses
            1f, // category_encoded
            Math.sin(2 * Math.PI * 2 / 7).toFloat(), // day_of_week_sin
            Math.cos(2 * Math.PI * 2 / 7).toFloat(), // day_of_week_cos
            0f, // is_weekend
            logAmount, // smoothed_expenses
            logAmount, // rolling_avg_7
            logAmount, // rolling_avg_30
            10f // additional_feature
        )

        val inputBuffer = ByteBuffer.allocateDirect(inputFeatures.size * 4)
        inputBuffer.order(ByteOrder.nativeOrder())
        for (feature in inputFeatures) {
            inputBuffer.putFloat(feature)
        }

        val outputBuffer = ByteBuffer.allocateDirect(4)
        outputBuffer.order(ByteOrder.nativeOrder())

        interpreter.run(inputBuffer, outputBuffer)

        outputBuffer.rewind()
        val predictionLog = outputBuffer.float
        return Math.expm1(predictionLog.toDouble()).toFloat() * 187.85f
    }

    fun close() {
        interpreter.close()
    }
}