package org.skilli.snaper.utils

import android.util.DisplayMetrics
import android.util.Size
import android.view.Display
import androidx.camera.core.*

object UsecaseConfigBuilder {

    fun buildPreviewConfig(display: Display): PreviewConfig {
        val metrics = DisplayMetrics().also { display.getMetrics(it) }
        return PreviewConfig.Builder()
            .setTargetRotation(display.rotation)
            .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
            .setLensFacing(CameraX.LensFacing.BACK)
            .build()
    }

    fun buildImageCaptureConfig(display: Display): ImageCaptureConfig {
        val metrics = DisplayMetrics().also { display.getMetrics(it) }
        return ImageCaptureConfig.Builder()
            .setTargetRotation(display.rotation)
            .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
            .setFlashMode(FlashMode.OFF)
            .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            .build()
    }
}