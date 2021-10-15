package org.skilli.snaper.repos.structure

import androidx.annotation.Keep
import org.skilli.snaper.utils.SnaperConstants
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Keep
data class ResponseData(
    val _id: String = UUID.randomUUID().toString(),
    val publishedAt: String = SimpleDateFormat(
        SnaperConstants.GIVEN_DATE_FORMAT,
        Locale.getDefault()
    ).format(Date()),
    val title: String = "This is a demo title",
    var picture: String = "",
    val comment: String = "This is a demo comment",
    var file: File? = null
)
