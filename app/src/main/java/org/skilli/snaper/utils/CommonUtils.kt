package org.skilli.snaper.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import org.skilli.snaper.BuildConfig
import java.io.File


fun Context.createTempFile(name: String? = System.currentTimeMillis().toString() + ".jpg"): File {
    val baseFile = applicationContext.cacheDir.absolutePath + "/TestPhoto/"
    File(baseFile).mkdir()

    return File(baseFile + name)
}

fun Context.getFileFromName(name: String): File {
    return File(applicationContext.cacheDir.absolutePath + "/TestPhoto/" + name)
}

fun Context.isFileExist(name: String, ext: String? = ".jpg"): Boolean {
    return File(applicationContext.cacheDir.absolutePath + "/TestPhoto/" + name + ext).exists()
}

infix fun Context.quickToast(msg: String?) =
    Toast.makeText(
        this,
        "$msg",
        if (msg == null || msg.length <= 20)
            Toast.LENGTH_SHORT
        else Toast.LENGTH_LONG
    ).show()

fun logit(msg: Any? = "...") {
    if (BuildConfig.DEBUG) {
        val trace: StackTraceElement? = Thread.currentThread().stackTrace[3]
        val lineNumber = trace?.lineNumber
        val methodName = trace?.methodName
        val className = trace?.fileName?.replaceAfter(".", "")?.replace(".", "")
        Log.d("Line $lineNumber", "$className::$methodName() -> $msg")
    }
}

fun Activity.immersiveMode() {
    window.statusBarColor = Color.TRANSPARENT
    window.navigationBarColor = Color.TRANSPARENT
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.setDecorFitsSystemWindows(false)
    }
}

fun Context.isInternetConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

        return when {

            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            else -> false
        }
    } else {
        return connectivityManager.activeNetworkInfo != null &&
                connectivityManager.activeNetworkInfo!!.isConnectedOrConnecting
    }
}