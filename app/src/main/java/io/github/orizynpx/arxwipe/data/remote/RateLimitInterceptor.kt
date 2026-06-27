package io.github.orizynpx.arxwipe.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicLong

class RateLimitInterceptor : Interceptor {
    private val lastRequestTime = AtomicLong(0)
    private val rateLimitMs = 3000L

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val host = request.url.host

        if (host == "export.arxiv.org") {
            synchronized(this) {
                val now = System.currentTimeMillis()
                val elapsed = now - lastRequestTime.get()
                if (elapsed < rateLimitMs) {
                    val sleepTime = rateLimitMs - elapsed
                    try {
                        Thread.sleep(sleepTime)
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }
                }
                lastRequestTime.set(System.currentTimeMillis())
            }
        }

        return chain.proceed(request)
    }
}
