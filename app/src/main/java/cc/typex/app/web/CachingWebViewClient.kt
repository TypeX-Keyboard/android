package cc.typex.app.web

import android.annotation.TargetApi
import android.os.Build
import android.util.LruCache
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class CachingWebViewClient() : WebViewClient() {

    companion object {
        // 缓存大小限制，这里设置为8MB
        private val cacheSize = 8 * 1024 * 1024

        // 使用LruCache存储缓存的资源
        private val memoryCache = object : LruCache<String, CachedResource>(cacheSize) {
            override fun sizeOf(key: String, value: CachedResource): Int {
                return value.data.size
            }
        }
    }

    // OkHttp客户端
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()


    // 存储缓存命中统计
    private val cacheHitCount = ConcurrentHashMap<String, Int>()

    // 资源缓存类
    data class CachedResource(
        val data: ByteArray,       // 资源数据
        val mimeType: String,      // MIME类型
        val encoding: String?,     // 编码
        val statusCode: Int,       // HTTP状态码
        val reasonPhrase: String,  // HTTP状态描述
        val responseHeaders: Map<String, String>?, // HTTP响应头
        val lastModified: Long     // 最后修改时间
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CachedResource

            if (!data.contentEquals(other.data)) return false
            if (mimeType != other.mimeType) return false
            if (encoding != other.encoding) return false
            if (statusCode != other.statusCode) return false
            if (reasonPhrase != other.reasonPhrase) return false
            if (responseHeaders != other.responseHeaders) return false
            if (lastModified != other.lastModified) return false

            return true
        }

        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + mimeType.hashCode()
            result = 31 * result + (encoding?.hashCode() ?: 0)
            result = 31 * result + statusCode
            result = 31 * result + reasonPhrase.hashCode()
            result = 31 * result + (responseHeaders?.hashCode() ?: 0)
            result = 31 * result + lastModified.hashCode()
            return result
        }
    }

    // 是否应该缓存资源
    private fun shouldCache(url: String): Boolean {
        // 可以根据需要自定义缓存策略
        // 例如，只缓存特定后缀的文件，或者只缓存特定域名的资源
        val lowerUrl = url.lowercase()
        return lowerUrl.endsWith(".js") ||
                lowerUrl.endsWith(".css") ||
                lowerUrl.endsWith(".png") ||
                lowerUrl.endsWith(".jpg") ||
                lowerUrl.endsWith(".jpeg") ||
                lowerUrl.endsWith(".gif") ||
                lowerUrl.endsWith(".webp") ||
                lowerUrl.endsWith(".svg") ||
                lowerUrl.endsWith(".ttf") ||
                lowerUrl.endsWith(".woff") ||
                lowerUrl.endsWith(".woff2")
    }

    // 将InputStream转换为ByteArray
    @Throws(IOException::class)
    private fun readBytes(inputStream: InputStream): ByteArray {
        val byteBuffer = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var len: Int
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }

    // API 21及以上调用
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val url = request.url.toString()

        // 检查是否应该缓存该资源
        if (shouldCache(url)) {
            // 尝试从缓存中获取资源
            val cachedResource = memoryCache.get(url)

            if (cachedResource != null) {
                // 缓存命中，更新统计
                val hitCount = cacheHitCount.getOrDefault(url, 0) + 1
                cacheHitCount[url] = hitCount

                // 返回缓存的资源
                val responseHeaders = cachedResource.responseHeaders ?: HashMap()
                return WebResourceResponse(
                    cachedResource.mimeType,
                    cachedResource.encoding,
                    ByteArrayInputStream(cachedResource.data)
                ).apply {
                    setStatusCodeAndReasonPhrase(
                        cachedResource.statusCode,
                        cachedResource.reasonPhrase
                    )
                    setResponseHeaders(responseHeaders)
                }
            }

            // 对于缓存未命中的情况，我们需要手动加载资源
            try {
                // 获取请求方法和headers
                val method = request.method ?: "GET"
                val headers = request.requestHeaders ?: HashMap()

                // 手动加载资源
                val (responseCode, responseMessage, responseHeaders, mimeType, encoding, data) =
                    loadResourceManually(url, method, headers)

                if (data != null) {
                    // 创建资源响应
                    val webResourceResponse = WebResourceResponse(
                        mimeType,
                        encoding,
                        ByteArrayInputStream(data)
                    ).apply {
                        setStatusCodeAndReasonPhrase(responseCode, responseMessage)
                        setResponseHeaders(responseHeaders)
                    }

                    // 缓存这个资源
                    val cachedResource = CachedResource(
                        data = data,
                        mimeType = mimeType,
                        encoding = encoding,
                        statusCode = responseCode,
                        reasonPhrase = responseMessage,
                        responseHeaders = responseHeaders,
                        lastModified = System.currentTimeMillis()
                    )
                    memoryCache.put(url, cachedResource)

                    return webResourceResponse
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 如果我们不处理或出错，交给系统默认处理
        return super.shouldInterceptRequest(view, request)
    }

    // 使用OkHttp手动加载资源的高级方法（支持HTTP方法和Headers）
    private fun loadResourceManually(
        url: String,
        method: String,
        headers: Map<String, String>
    ): ResourceResponse {
        try {
            // 创建请求
            val requestBuilder = Request.Builder()
                .url(url)
                .method(
                    method,
                    if (method == "GET" || method == "HEAD") null else okhttp3.RequestBody.create(
                        null,
                        ByteArray(0)
                    )
                )

            // 添加请求头
            for ((key, value) in headers) {
                requestBuilder.addHeader(key, value)
            }

            // 执行请求
            val response = okHttpClient.newCall(requestBuilder.build()).execute()
            val responseCode = response.code
            val responseMessage = response.message

            // 收集响应头
            val responseHeaders = HashMap<String, String>()
            for (name in response.headers.names()) {
                val value = response.header(name)
                if (value != null) {
                    responseHeaders[name] = value
                }
            }

            // 获取响应内容
            if (response.isSuccessful) {
                val responseBody = response.body
                if (responseBody != null) {
                    val data = responseBody.bytes()
                    val mimeType = response.header("Content-Type") ?: guessMimeTypeFromUrl(url)
                    val encoding = response.header("Content-Encoding") ?: "UTF-8"

                    return ResourceResponse(
                        responseCode,
                        responseMessage,
                        responseHeaders,
                        mimeType,
                        encoding,
                        data
                    )
                }
            }

            // 返回错误响应
            return ResourceResponse(
                responseCode,
                responseMessage,
                responseHeaders,
                "text/plain",
                "UTF-8",
                null
            )

        } catch (e: Exception) {
            e.printStackTrace()
            return ResourceResponse(
                500,
                "Internal Error",
                HashMap(),
                "text/plain",
                "UTF-8",
                null
            )
        }
    }

    // 资源响应类
    data class ResourceResponse(
        val statusCode: Int,
        val reasonPhrase: String,
        val headers: Map<String, String>,
        val mimeType: String,
        val encoding: String,
        val data: ByteArray?
    )

    // API 21以下调用
    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        // 检查是否应该缓存该资源
        if (shouldCache(url)) {
            // 尝试从缓存中获取资源
            val cachedResource = memoryCache.get(url)

            if (cachedResource != null) {
                // 缓存命中，更新统计
                val hitCount = cacheHitCount.getOrDefault(url, 0) + 1
                cacheHitCount[url] = hitCount

                // 返回缓存的资源
                return WebResourceResponse(
                    cachedResource.mimeType,
                    cachedResource.encoding,
                    ByteArrayInputStream(cachedResource.data)
                )
            }

            // 对于缓存未命中的情况，让默认的WebView处理加载
            // 但我们自己不能获取到加载结果，所以无法缓存
            // 在API 21以下，我们可以创建一个自定义的加载方式来获取资源
            try {
                // 使用自己的方式加载资源（这里需要具体实现）
                val (mimeType, content) = loadResourceManually(url)
                if (content != null) {
                    // 创建资源响应
                    val webResourceResponse = WebResourceResponse(
                        mimeType,
                        "UTF-8",
                        ByteArrayInputStream(content)
                    )

                    // 缓存这个资源
                    val cachedResource = CachedResource(
                        data = content,
                        mimeType = mimeType,
                        encoding = "UTF-8",
                        statusCode = 200,
                        reasonPhrase = "OK",
                        responseHeaders = null,
                        lastModified = System.currentTimeMillis()
                    )
                    memoryCache.put(url, cachedResource)

                    return webResourceResponse
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 如果我们不处理或出错，交给系统默认处理
        return super.shouldInterceptRequest(view, url)
    }

    // 使用OkHttp手动加载资源的方法
    private fun loadResourceManually(url: String): Pair<String, ByteArray?> {
        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body
                if (responseBody != null) {
                    val data = responseBody.bytes()
                    val mimeType = response.header("Content-Type") ?: guessMimeTypeFromUrl(url)
                    return Pair(mimeType, data)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair("application/octet-stream", null)
    }

    // 根据URL猜测MIME类型
    private fun guessMimeTypeFromUrl(url: String): String {
        return when {
            url.endsWith(".html", true) -> "text/html"
            url.endsWith(".js", true) -> "application/javascript"
            url.endsWith(".css", true) -> "text/css"
            url.endsWith(".jpg", true) || url.endsWith(".jpeg", true) -> "image/jpeg"
            url.endsWith(".png", true) -> "image/png"
            url.endsWith(".gif", true) -> "image/gif"
            url.endsWith(".svg", true) -> "image/svg+xml"
            url.endsWith(".json", true) -> "application/json"
            url.endsWith(".ttf", true) -> "font/ttf"
            url.endsWith(".woff", true) -> "font/woff"
            url.endsWith(".woff2", true) -> "font/woff2"
            else -> "application/octet-stream"
        }
    }

    // 清除缓存
    fun clearCache() {
        memoryCache.evictAll()
        cacheHitCount.clear()
    }

    // 获取缓存统计信息
    fun getCacheStats(): Map<String, Any> {
        return mapOf(
            "cacheSize" to memoryCache.size(),
            "maxCacheSize" to memoryCache.maxSize(),
            "hitCount" to cacheHitCount,
            "putCount" to memoryCache.putCount(),
            "evictionCount" to memoryCache.evictionCount()
        )
    }
}