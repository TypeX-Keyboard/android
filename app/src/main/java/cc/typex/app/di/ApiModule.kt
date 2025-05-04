package cc.typex.app.di

import cc.typex.app.api.ApiResponseConverterFactory
import cc.typex.app.api.ApiService
import cc.typex.app.api.interceptor.ApiCommonParamInterceptor
import cc.typex.app.api.interceptor.ApiEncryptionInterceptor
import cc.typex.app.api.interceptor.ApiSignParameterInterceptor
import cc.typex.app.util.EnvManager
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    fun provideHttpLoggingInterceptor(
        envManager: EnvManager,
    ): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = envManager.getHttpClientLogLevel()
        return interceptor
    }

    @Provides
    fun provideOkHttpClient(
        commonParams: ApiCommonParamInterceptor,
        logging: HttpLoggingInterceptor,
        bodyEncryption: ApiEncryptionInterceptor,
        paramSign: ApiSignParameterInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(commonParams)
            .addInterceptor(logging)
            .addInterceptor(bodyEncryption)
            .addInterceptor(paramSign)
            .build()
    }

    @Provides
    fun provideRetrofit(
        envManager: EnvManager,
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(envManager.getApiHost())
            .client(okHttpClient)
            .addConverterFactory(ApiResponseConverterFactory(GsonConverterFactory.create(gson)))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(
        retrofit: Retrofit
    ): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}