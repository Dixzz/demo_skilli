package org.skilli.snaper.base

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

open class BaseActivity<B : ViewBinding>(private val bindingFactory: (LayoutInflater) -> B) :
    AppCompatActivity() {
    val binding: B by lazy { bindingFactory(layoutInflater) }
    val handler by lazy { Handler(Looper.getMainLooper()) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}