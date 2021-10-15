package org.skilli.snaper.activities

import android.Manifest
import android.content.Intent
import android.graphics.Matrix
import android.util.Pair
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import androidx.camera.core.CameraX
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.squareup.picasso.Picasso
import org.androidannotations.annotations.AfterViews
import org.androidannotations.annotations.Bean
import org.androidannotations.annotations.EActivity
import org.skilli.snaper.api.NetworkRequest
import org.skilli.snaper.base.BaseActivity
import org.skilli.snaper.databinding.ActivityCameraBinding
import org.skilli.snaper.utils.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import java.io.File
import java.util.concurrent.Executors


@EActivity
@RuntimePermissions
open class CameraActivity : BaseActivity<ActivityCameraBinding>(ActivityCameraBinding::inflate) {
    private var fileInMemory: File? = null
    private lateinit var preview: Preview
    private lateinit var capture: ImageCapture

    @Bean
    lateinit var networkRequest: NetworkRequest

    @AfterViews
    open fun initViews() {
        immersiveMode()
        binding.retake.hide()
        binding.accept.hide()
        binding.viewFinder.post {
            setUpCamera()
        }
    }

    override fun onDestroy() {
        CameraX.unbindAll()
        super.onDestroy()
    }

    private fun buildPreviewUseCase(): Preview {
        preview = Preview(
            UsecaseConfigBuilder.buildPreviewConfig(
                binding.viewFinder.display
            )
        )
        preview.setOnPreviewOutputUpdateListener { previewOutput ->
            updateViewFinderWithPreview(previewOutput)
            correctPreviewOutputForDisplay(previewOutput.textureSize)
        }

        return preview
    }

    private fun updateViewFinderWithPreview(previewOutput: Preview.PreviewOutput) {
        val parent = binding.viewFinder.parent as ViewGroup
        parent.removeView(binding.viewFinder)
        parent.addView(binding.viewFinder, 0)
        binding.viewFinder.setSurfaceTexture(previewOutput.surfaceTexture)
    }

    private fun correctPreviewOutputForDisplay(textureSize: Size) {
        val matrix = Matrix()

        val centerX = binding.viewFinder.width / 2f
        val centerY = binding.viewFinder.height / 2f

        val displayRotation = getDisplayRotation()
        val (dx, dy) = getDisplayScalingFactors(textureSize)

        matrix.postRotate(displayRotation, centerX, centerY)
        matrix.preScale(dx, dy, centerX, centerY)

        // Correct preview output to account for display rotation and scaling
        binding.viewFinder.setTransform(matrix)
    }

    private fun getDisplayRotation(): Float {
        val rotationDegrees = when (binding.viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> throw IllegalStateException("Unknown display rotation ${binding.viewFinder.display.rotation}")
        }
        return -rotationDegrees.toFloat()
    }

    private fun getDisplayScalingFactors(textureSize: Size): Pair<Float, Float> {
        val cameraPreviewRation = textureSize.height / textureSize.width.toFloat()
        val scaledWidth: Int
        val scaledHeight: Int
        if (binding.viewFinder.width > binding.viewFinder.height) {
            scaledHeight = binding.viewFinder.width
            scaledWidth = (binding.viewFinder.width * cameraPreviewRation).toInt()
        } else {
            scaledHeight = binding.viewFinder.height
            scaledWidth = (binding.viewFinder.height * cameraPreviewRation).toInt()
        }
        val dx = scaledWidth / binding.viewFinder.width.toFloat()
        val dy = scaledHeight / binding.viewFinder.height.toFloat()
        return Pair(dx, dy)
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    open fun onPermissionDeniedCamera() {
        this quickToast "Permission denied exiting"
        finish()
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    open fun setUpCamera() {
        CameraX.unbindAll()
        CameraX.bindToLifecycle(
            this,
            buildPreviewUseCase(),
            buildImageCaptureUseCase(),
        )

        networkRequest.viewmodel.fileData.value = null
        val poolExecutor = Executors.newSingleThreadExecutor()
        networkRequest.viewmodel.fileData.observe(this) {
            logit(it)
            if (it == null)
                return@observe
            LocalBroadcastManager.getInstance(this.applicationContext)
                .sendBroadcast(Intent(SnaperConstants.IntentKeys.KEY_LOCAL_BR))
            Picasso.get().load(it).centerCrop()
                .resize(binding.previewImage.width, binding.previewImage.height)
                .into(binding.previewImage)

            /* bkp method
            * Currently commented to not generate unused methods */
            /*binding.previewImage.setImageBitmap(
                decodeSampledBitmapFromResource(
                    it,
                    binding.previewImage
                )
            )*/
        }
        binding.retake.setOnClickListener {
            fileInMemory?.delete()
            binding.fabCam.show()
            binding.retake.hide()
            binding.accept.hide()
            binding.previewImage.setImageBitmap(null)
        }
        binding.accept.setOnClickListener {
            finish()
            this quickToast "Image Saved"
            handler.post { networkRequest.viewmodel.fileData.value = fileInMemory }
        }
        binding.fabCam.setOnClickListener {
            capture.takePicture(
                this.createTempFile(),
                poolExecutor,
                object : ImageCapture.OnImageSavedListener {
                    override fun onImageSaved(file: File) {
                        fileInMemory = file
                        logit(file)
                        handler.post {
                            Picasso.get().load(file).centerCrop()
                                .resize(binding.previewImage.width, binding.previewImage.height)
                                .into(binding.previewImage)
                            binding.fabCam.hide()
                            binding.retake.show()
                            binding.accept.show()
                        }
                    }

                    override fun onError(
                        imageCaptureError: ImageCapture.ImageCaptureError,
                        message: String,
                        cause: Throwable?
                    ) {
                        this@CameraActivity quickToast message
                        cause?.printStackTrace()
                    }

                })
        }
    }

    //image capture action
    private fun buildImageCaptureUseCase(): ImageCapture {
        capture = ImageCapture(
            UsecaseConfigBuilder.buildImageCaptureConfig(
                binding.viewFinder.display
            )
        )
        return capture
    }
}

/*fun Bitmap.rotate(): Bitmap {
    val matrix = Matrix().apply {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            postRotate(90f)
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    // Raw height and width of image
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

fun decodeSampledBitmapFromResource(
    file: File,
    imageView: ImageView,
): Bitmap {
    // First decode with inJustDecodeBounds=true to check dimensions
    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, this)

        // Calculate inSampleSize
        inSampleSize = calculateInSampleSize(this, imageView.width, imageView.height)

        // Decode bitmap with inSampleSize set
        inJustDecodeBounds = false

        BitmapFactory.decodeFile(file.absolutePath, this).rotate()
    }
}*/