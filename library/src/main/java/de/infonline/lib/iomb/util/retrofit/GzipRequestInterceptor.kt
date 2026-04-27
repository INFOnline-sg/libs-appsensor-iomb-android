package de.infonline.lib.iomb.util.retrofit

import okhttp3.*
import okio.BufferedSink
import okio.GzipSink
import okio.IOException
import okio.buffer

// https://github.com/square/okhttp/blob/fea8fbba5fd9eadf3f88b91c1479290a60e6d462/samples/guide/src/main/java/okhttp3/recipes/RequestBodyCompression.java#L73
internal class GzipRequestInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        if (originalRequest.body == null || originalRequest.header("Content-Encoding") != null) {
            return chain.proceed(originalRequest)
        }
        val compressedRequest: Request = originalRequest.newBuilder()
                .header("Content-Encoding", "gzip")
                .method(originalRequest.method, originalRequest.body?.gzip())
                .build()
        return chain.proceed(compressedRequest)
    }

    private fun RequestBody?.gzip(): RequestBody? = object : RequestBody() {
        override fun contentType(): MediaType? = this@gzip?.contentType()

        override fun contentLength(): Long = -1 // We don't know the compressed length in advance!

        @Throws(IOException::class)
        override fun writeTo(sink: BufferedSink) {
            if (this@gzip == null) return
            GzipSink(sink).buffer().use {
                this@gzip.writeTo(it)
                it.close()
            }
        }
    }
}