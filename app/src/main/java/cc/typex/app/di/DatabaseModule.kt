package cc.typex.app.di

import android.content.Context
import androidx.room.Room
import cc.typex.app.db.AppDatabase
import cc.typex.app.db.EntityDao
import cc.typex.app.db.TokenHistoryDao
import cc.typex.app.db.WalletDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        val name = AppDatabase.DATABASE_NAME
        return Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    @Singleton
    fun provideEntityDao(
        database: AppDatabase
    ): EntityDao {
        return database.entityDao()
    }

    @Provides
    @Singleton
    fun provideWalletDao(
        database: AppDatabase
    ): WalletDao {
        return database.walletDao()
    }

    @Provides
    @Singleton
    fun provideTokenHistoryDao(
        database: AppDatabase
    ): TokenHistoryDao {
        return database.tokenHistoryDao()
    }
}