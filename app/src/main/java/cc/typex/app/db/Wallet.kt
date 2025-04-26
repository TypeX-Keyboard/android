package cc.typex.app.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.rxjava3.core.Single

@Entity
data class Wallet(
    @PrimaryKey
    val address: String,
    val alias: String,
    val selected: Boolean,
    val order: Int,
    val mnemonic: Boolean,
    val keys: ByteArray,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Wallet

        if (address != other.address) return false
        if (alias != other.alias) return false
        if (selected != other.selected) return false
        if (order != other.order) return false
        if (mnemonic != other.mnemonic) return false
        if (!keys.contentEquals(other.keys)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + alias.hashCode()
        result = 31 * result + selected.hashCode()
        result = 31 * result + order
        result = 31 * result + mnemonic.hashCode()
        result = 31 * result + keys.contentHashCode()
        return result
    }
}

@Dao
interface WalletDao {

    @Query("SELECT * FROM wallet WHERE `address`=:address;")
    fun queryByAddressSync(address: String): Wallet?

    @Query("SELECT COUNT(*) FROM wallet;")
    fun countObservable(): Single<Int>

    @Query("SELECT COUNT(*) FROM wallet;")
    fun countSync(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertSync(wallet: Wallet)

    @Query("SELECT * FROM wallet WHERE `selected`=1;")
    fun getDefaultWallet(): Wallet?

    @Query("SELECT * FROM wallet WHERE `address`=:address;")
    fun getWalletByAddress(address: String): Wallet?

    @Query("SELECT alias FROM wallet")
    fun getAliasList(): List<String>

    @Query("DELETE FROM wallet WHERE `address`=:address;")
    fun deleteWalletByAddress(address: String)

    @Query("SELECT * FROM wallet ORDER BY `order` ASC;")
    fun getWalletList(): List<Wallet>

    @Query("UPDATE wallet SET `selected`=:selected WHERE `address`=:address;")
    fun updateWalletDefault(address: String, selected: Boolean)

    @Query("SELECT * FROM wallet ORDER BY `order` ASC")
    fun queryAllPaging(): PagingSource<Int, Wallet>
}