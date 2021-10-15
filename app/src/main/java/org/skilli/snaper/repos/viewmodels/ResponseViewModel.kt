package org.skilli.snaper.repos.viewmodels

import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.skilli.snaper.repos.structure.ResponseData
import java.io.File
@Keep
class ResponseViewModel : ViewModel() {
    val listOfData = MutableLiveData<ArrayList<ResponseData>>()
    val listOfResolvedData = MutableLiveData<ArrayList<ResponseData>>()
    val currentImage = MutableLiveData<Drawable>()
    val isDataLoaded = MutableLiveData(false)
    val fileData = MutableLiveData<File>()
}