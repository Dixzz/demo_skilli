package org.skilli.snaper.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.Slide
import androidx.transition.TransitionManager
import okhttp3.*
import org.androidannotations.annotations.AfterInject
import org.androidannotations.annotations.AfterViews
import org.androidannotations.annotations.Bean
import org.androidannotations.annotations.EActivity
import org.skilli.snaper.R
import org.skilli.snaper.api.NetworkRequest
import org.skilli.snaper.base.BaseActivity
import org.skilli.snaper.base.BaseRecyclerViewAdapter
import org.skilli.snaper.databinding.ActivityMainBinding
import org.skilli.snaper.databinding.GridOnewBinding
import org.skilli.snaper.repos.structure.ResponseData
import org.skilli.snaper.utils.CallbackRetro
import org.skilli.snaper.utils.SnaperConstants
import org.skilli.snaper.utils.immersiveMode
import org.skilli.snaper.utils.logit
import java.util.*
import kotlin.collections.ArrayList


@EActivity
open class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    @Bean
    lateinit var networkRequest: NetworkRequest

    var requestList = ArrayList<retrofit2.Call<ResponseBody>>()
    var itemPositionClicked = 0

    override fun onDestroy() {
        SnaperConstants.BundleKeys.KEY_ITEM_POS
        requestList.forEach {
            it.cancel()
        }
        super.onDestroy()
    }

    @AfterInject
    fun init() {
        networkRequest.fetchData()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SnaperConstants.BundleKeys.KEY_ITEM_POS, itemPositionClicked)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        immersiveMode()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility += View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.statusBarColor = Color.BLACK
        }
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            itemPositionClicked = it.getInt(SnaperConstants.BundleKeys.KEY_ITEM_POS)
        }
    }

    @AfterViews
    fun initViews() {
        //throw  RuntimeException("Boom!");


        binding.recycler.layoutManager = GridLayoutManager(this, 2)
        val adapter = BaseRecyclerViewAdapter(
            list = ArrayList<ResponseData>(),
            bindingFactory = GridOnewBinding::inflate,
            onClickI = object : BaseRecyclerViewAdapter.OnItemClickI {
                override fun click(pos: Int, v: View) {
                    itemPositionClicked = pos
                    val childImg = v.findViewById<ImageView>(R.id.img)
                    val options = ActivityOptionsCompat.makeCustomAnimation(
                        this@MainActivity,
                        R.anim.test,
                        0
                    )
                    networkRequest.viewmodel.currentImage.value =
                        childImg.drawable

                    // FIXME: 10/14/2021
                    /* startActivity(Intent intent, @RecentlyNullable Bundle options)
                    * For some reason animation is turning to null and screen freezes out on change of orientation */
                    startActivity(
                        Intent(this@MainActivity, PhotoViewActivity_::class.java).putExtra(
                            "pos",
                            itemPositionClicked
                        ), options.toBundle()
                    )
                }
            }
        ).apply { setHasStableIds(true) }
        binding.recycler.adapter = adapter

        LocalBroadcastManager.getInstance(this.applicationContext).registerReceiver(object :
            BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                logit("Here ${networkRequest.viewmodel.listOfResolvedData.value?.size} ${networkRequest.viewmodel.fileData.value}")
                if (networkRequest.viewmodel.fileData.value == null || networkRequest.viewmodel.listOfResolvedData.value == null)
                    return

                if (networkRequest.viewmodel.fileData.value!! != networkRequest.viewmodel.listOfResolvedData.value!!.last().file) {
                    networkRequest.viewmodel.listOfResolvedData.value!!.add(
                        ResponseData(
                            file = networkRequest.viewmodel.fileData.value!!
                        )
                    )
                    adapter.notifyItemInserted(adapter.itemCount)
                    binding.recycler.smoothScrollToPosition(networkRequest.viewmodel.listOfResolvedData.value!!.size)
                    logit("Here ${networkRequest.viewmodel.listOfResolvedData.value?.size}")
                }
            }
        }, IntentFilter(SnaperConstants.IntentKeys.KEY_LOCAL_BR))

        networkRequest.viewmodel.listOfResolvedData.observe(this) {
            logit(it.size)
            if (!networkRequest.viewmodel.isDataLoaded.value!!) {
                adapter.update(it)
                networkRequest.viewmodel.isDataLoaded.value = true
                updateViewsOnceDataReceived()
            }
        }


        updateViewsOnceDataLoading()
        binding.fabCam.setOnClickListener {
            startActivity(Intent(this, CameraActivity_::class.java))
        }
        networkRequest.viewmodel.listOfData.observe(this) { responseList ->
            logit(networkRequest.viewmodel.isDataLoaded.value)
            if (!networkRequest.viewmodel.isDataLoaded.value!!) {
                logit("List=${responseList.size}")
                val total = System.currentTimeMillis()
                if (!binding.statusShimmer.isShimmerVisible) {
                    updateViewsOnceDataLoading()
                }
                logit(networkRequest.viewmodel.isDataLoaded.value)
                if (responseList.isNotEmpty() && responseList.first().picture == SnaperConstants.IMAGE_URL + "600/300/?random") {
                    responseList.forEach {
                        networkRequest.imageClient.generatePics(System.currentTimeMillis()).run {
                            requestList.add(this)
                            enqueue(CallbackRetro<ResponseBody, ResponseBody>().addQuickCall { response ->
                                val link = response.raw().request.url.toString()
                                it.picture = link
                                if (responseList.last() == it) {
                                    logit("Time took: ${System.currentTimeMillis() - total}")
                                    networkRequest.viewmodel.listOfResolvedData.value = responseList
                                }
                            })
                        }
                    }
                } else {
                    networkRequest.viewmodel.listOfResolvedData.value = responseList
                }
            } else {
                adapter.update(networkRequest.viewmodel.listOfResolvedData.value)
                updateViewsOnceDataReceived()
            }
        }
    }

    private fun updateViewsOnceDataLoading() {
        binding.status.text = "Loading..."
        binding.fabCam.hide()
        binding.statusShimmer.showShimmer(true)
    }

    private fun updateViewsOnceDataReceived() {
        binding.statusShimmer.hideShimmer()
        binding.status.text = "Gallery"
        binding.fabCam.show()
        binding.recycler.scheduleLayoutAnimation()
        TransitionManager.beginDelayedTransition(
            binding.lottieView.parent as ViewGroup,
            Slide(Gravity.BOTTOM)
        )
        binding.lottieView.visibility = View.GONE
        binding.lottieView.cancelAnimation()
    }
}


/* Raw HTTP response slow compared to retrofit by 43.279% */
/*postguy(it.picture, object : Callback {
    override fun onFailure(call: Call, e: IOException) {

    }

    override fun onResponse(call: Call, response: okhttp3.Response) {
        response.use { r ->
            val link = r.request.url.toString()
            logit(link)
            it.picture = link
            //temp.add(it)
            networkRequest.viewmodel.responseItem.postValue(it)
            //list.add(it)
            *//*if (responseList.size == temp.size) {
                                logit(System.currentTimeMillis() - total)
                                networkRequest.viewmodel.listOfResolvedData.value  = (temp)
                                networkRequest.viewmodel.listOfData.value?.clear()
                            }*//*
                        }
                    }
                })*/
/*private fun createCachedClient(): OkHttpClient {
    val okHttpClient = OkHttpClient.Builder()
    return okHttpClient.build()
}

private val cachedClient by lazy { createCachedClient() }
fun postguy(url: String, callback: Callback) {
    val request: Request = Request.Builder()
        .url(url)
        .get()
        .build()

    val call: Call = cachedClient.newCall(request)

    call.enqueue(callback)
}*/