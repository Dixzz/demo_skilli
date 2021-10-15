package org.skilli.snaper.utils

import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback

open class CallbackRetro<T, Y>(val errorClass: Class<Y>? = null) {
    companion object;

    inline fun addQuickCall(
        crossinline criticalErrorFunction: (throwable: Throwable) -> Unit = {},
        crossinline responseErrorFunction: (responseError: Y) -> Unit = {},
        crossinline responseFunction: (response: retrofit2.Response<T>) -> Unit = {},
    ): Callback<T> {

        return object : Callback<T> {
            override fun onResponse(call: Call<T>, response: retrofit2.Response<T>) {
                try {
                    if (response.isSuccessful)
                        responseFunction.invoke(response)
                    else {
                        if (errorClass == null)
                            logit(response.errorBody())
                        else
                            responseErrorFunction.invoke(
                                Gson().newBuilder().disableHtmlEscaping().create().fromJson(
                                    response.errorBody()?.string(),
                                    errorClass
                                )
                            )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    criticalErrorFunction(Throwable("Error parsing response"))
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                t.printStackTrace()
                criticalErrorFunction.invoke(t)
            }
        }
    }
}