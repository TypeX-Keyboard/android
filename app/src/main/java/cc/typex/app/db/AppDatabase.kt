package cc.typex.app.db


import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    version = 1,
    entities = [Entity::class, Wallet::class, TokenHistory::class],
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun entityDao(): EntityDao

    abstract fun walletDao(): WalletDao

    abstract fun tokenHistoryDao(): TokenHistoryDao

    companion object {
        const val DATABASE_NAME = "app_database"
    }
}