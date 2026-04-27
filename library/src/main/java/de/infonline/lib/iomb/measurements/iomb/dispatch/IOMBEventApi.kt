package de.infonline.lib.iomb.measurements.iomb.dispatch

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

internal interface IOMBEventApi {

    @Headers("Content-Type: application/json")
    @POST
    fun postEvent(
        @Url url: String,
        @Body event: RequestBody
    ): Call<Void>
}