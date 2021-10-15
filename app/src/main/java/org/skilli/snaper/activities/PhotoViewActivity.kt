package org.skilli.snaper.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import eightbitlab.com.blurview.RenderScriptBlur
import org.androidannotations.annotations.AfterViews
import org.androidannotations.annotations.Bean
import org.androidannotations.annotations.EActivity
import org.skilli.snaper.BuildConfig
import org.skilli.snaper.api.NetworkRequest
import org.skilli.snaper.base.BaseActivity
import org.skilli.snaper.databinding.ActivityPhotoViewBinding
import org.skilli.snaper.utils.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@EActivity
open class PhotoViewActivity :
    BaseActivity<ActivityPhotoViewBinding>(ActivityPhotoViewBinding::inflate) {

    @Bean
    lateinit var networkRequest: NetworkRequest

    private val givenFormat =
        SimpleDateFormat(SnaperConstants.GIVEN_DATE_FORMAT, Locale.getDefault())
    private val beautyFormat = SimpleDateFormat("dd, MMM yyyy", Locale.getDefault())

    @AfterViews
    fun initViews() {
        binding.blurView.setupWith(binding.root as ViewGroup).run {
            setBlurAutoUpdate(false)
            setHasFixedTransformationMatrix(true)
            setBlurEnabled(true).setBlurRadius(20f)
            setBlurAlgorithm(RenderScriptBlur(this@PhotoViewActivity))
        }

        val itemPositionClicked = intent.extras!!.getInt("pos", 0)
        networkRequest.viewmodel.currentImage.observe(this) {
            if (it != null)
                binding.image.setImageDrawable(it)
        }

        networkRequest.viewmodel.listOfResolvedData.value?.let {
            val item = it[itemPositionClicked]
            binding.data = item
            try {
                binding.dateView.text = beautyFormat.format(givenFormat.parse(item.publishedAt))
            } catch (_: Exception) {
            }
            binding.shareBtn.setOnClickListener {
                if (!this.isFileExist(item._id)) {
                    val f = this.createTempFile(item._id + ".jpg")
                    logit("File ${f.exists()} $f")
                    val bmp =
                        networkRequest.viewmodel.currentImage.value!!.toBitmap(
                            binding.image.width,
                            binding.image.height
                        )

                    FileOutputStream(f).use {
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }
                    shareFile(f)
                } else {
                    shareFile(getFileFromName(item._id + ".jpg"))
                }
            }
        }
    }

    private fun shareFile(f: File) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_STREAM,
            FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + ".provider",
                f
            )
        )
        sendIntent.type = "image/*"
        startActivity(
            Intent.createChooser(
                sendIntent,
                "Share via"
            )
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        immersiveMode()
        super.onCreate(savedInstanceState)
    }
}