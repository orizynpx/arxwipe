package io.github.orizynpx.arxwipe.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
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

        var response = chain.proceed(request)
        
        
        if (response.code == 429 && host == "export.arxiv.org") {
            Timber.w("Received HTTP 429 from arXiv. Waiting before retry...")
            response.close()
            
            
            try {
                Thread.sleep(5000)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            
            
            lastRequestTime.set(System.currentTimeMillis())
            
            
            response = chain.proceed(request)
        }

        return response
    }
}
