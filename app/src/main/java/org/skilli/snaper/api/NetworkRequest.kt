package org.skilli.snaper.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.androidannotations.annotations.EBean
import org.skilli.snaper.BuildConfig
import org.skilli.snaper.repos.structure.ResponseData
import org.skilli.snaper.repos.viewmodels.ResponseViewModel
import org.skilli.snaper.utils.CallbackRetro
import org.skilli.snaper.utils.SnaperConstants
import org.skilli.snaper.utils.logit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


@EBean(scope = EBean.Scope.Singleton)
open class NetworkRequest {
    /*@RootContext
    lateinit var context: Context*/

    private val apiClient by lazy {
        invoke(SnaperConstants.ROOT_URL)
    }
    val imageClient by lazy {
        invoke(SnaperConstants.IMAGE_URL)
    }
    val viewmodel by lazy {
        ResponseViewModel()
    }


    fun fetchData() {
        logit(viewmodel.listOfData.value)
        apiClient.responseData()
            .enqueue(CallbackRetro<ArrayList<ResponseData>, ResponseData>().addQuickCall {
                it.body()?.let {
                    viewmodel.listOfData.postValue(it.apply {
                        /*sortedByDescending {
                            it.publishedAt
                        }*/
                    })
                }
            })
    }

    private val cacheInterceptor by lazy {
        Interceptor { chain ->
            val response: okhttp3.Response = chain.proceed(chain.request())

            //logit(isInternetConnected())
            val cacheControlHeader: String = if (/*isInternetConnected()*/false) {
                val maxAge = 2419200
                "public, max-age=$maxAge"
            } else {
                //int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
                val maxStale = 2419200
                "public, only-if-cached, max-stale=$maxStale"
            }
            response.newBuilder()
                .removeHeader("Pragma") // HTTP/1.0
                .removeHeader("Cache-Control") // HTTP/1.1
                .header("Cache-Control", cacheControlHeader)
                .build()
        }
    }
    val httpClient by lazy {
        OkHttpClient.Builder().run {
            if (BuildConfig.DEBUG) { //Set logging of server response and request
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BODY
                addInterceptor(logging)
            }
            /*cache(Cache(File(context.cacheDir, "network_cache"), 10 * 1024 * 1024))
            addNetworkInterceptor(cacheInterceptor)
            addInterceptor(cacheInterceptor)*/
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(2, TimeUnit.MINUTES)
            build()
        }
    }

    private operator fun invoke(baseUrl: String): Api =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Api::class.java)
}