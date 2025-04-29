package cc.typex.app.util

import cc.typex.app.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnvManager @Inject constructor(
    private val webAssetsManager: WebAssetsManager
) {
    val isDebugEnv = BuildConfig.DEBUG
    private val isDevEnv = BuildConfig.DEV
    private val isWebLocalMode = true

    fun getApiHost(): String {
        return if (isDevEnv) {
            "https://eve.luckeyboard.com"
        } else {
            "https://api.typex.cc"
        }
    }

    private fun getWebHost(): String {
        return if (isDevEnv) {
            "https://eve-h5.luckeyboard.com"
        } else {
            "https://web.typex.cc"
        }
    }

    fun getWebHomeUrl(): String {
        return if (isWebLocalMode && webAssetsManager.isReady()) {
            "file://${webAssetsManager.getWebIndexFileUrl()}#/app"
        } else {
            "${getWebHost()}/#/app"
        }
    }

    fun getWebWalletUrl(): String {
        return if (isWebLocalMode && webAssetsManager.isReady()) {
            "file://${webAssetsManager.getWebIndexFileUrl()}"
        } else {
            "${getWebHost()}/"
        }
    }

    fun getWebHistoryUrl(): String {
        return if (isWebLocalMode && webAssetsManager.isReady()) {
            "file://${webAssetsManager.getWebIndexFileUrl()}#/recent"
        } else {
            "${getWebHost()}/#/recent"
        }
    }

    fun getWebSearchUrl(text: String): String {
        return if (isWebLocalMode && webAssetsManager.isReady()) {
            "file://${webAssetsManager.getWebIndexFileUrl()}#/check?text=$text"
        } else {
            "${getWebHost()}/#/check?text=$text"
        }
    }

    fun getPrivacyPolicyUrl(): String {
        return "${getWebHost()}/#/privacy"
    }

    fun getTermsOfServiceUrl(): String {
        return "${getWebHost()}/#/terms"
    }

    fun getHttpClientLogLevel(): HttpLoggingInterceptor.Level {
        return if (isDebugEnv) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
}