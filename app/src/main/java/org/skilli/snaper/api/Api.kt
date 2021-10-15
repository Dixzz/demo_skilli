package org.skilli.snaper.api

import okhttp3.ResponseBody
import org.skilli.snaper.repos.structure.ResponseData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {

    @GET("b/60cb2fa78a4cd025b79f18c8")
    fun responseData(): Call<ArrayList<ResponseData>>

    /*Response is slow */
    @GET("600/300/?random")
    fun generatePics(
        @Query("t") date: Long
    ): Call<ResponseBody>
}