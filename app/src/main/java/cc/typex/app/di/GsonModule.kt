package cc.typex.app.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class GsonModule {

    @Provides
    @Singleton
    fun provideGsonSingleton(): Gson {
        return GsonBuilder()
            .create()
    }
}