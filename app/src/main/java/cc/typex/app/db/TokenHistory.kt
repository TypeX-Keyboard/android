package cc.typex.app.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Entity
data class TokenHistory(
    @PrimaryKey
    val address: String,
    val timestamp: Long,
)

@Dao
interface TokenHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: TokenHistory): Completable

    @Query("SELECT * FROM TokenHistory ORDER BY timestamp DESC LIMIT 20")
    fun getAll(): Single<List<TokenHistory>>
}