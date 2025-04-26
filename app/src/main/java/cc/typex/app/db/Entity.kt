package cc.typex.app.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import androidx.room.Entity as RoomEntityAnnotation

@RoomEntityAnnotation
data class Entity(
    @PrimaryKey
    val key: String,
    val value: String,
)

@Dao
interface EntityDao {

    @Query("SELECT * FROM entity WHERE `key`=:key;")
    fun query(key: String): Single<Entity>

    @Query("SELECT * FROM entity WHERE `key`=:key;")
    fun querySync(key: String): Entity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: Entity): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSync(entity: Entity)

    @Query("DELETE FROM entity WHERE `key`=:key;")
    fun deleteSync(key: String)

    fun set(key: String, value: String): Completable {
        return insert(Entity(key, value))
    }

    fun get(key: String): Single<String> {
        return query(key).map { it.value }
    }

    fun setSync(key: String, value: String) {
        insertSync(Entity(key, value))
    }

    fun getSync(key: String): String? {
        return querySync(key)?.value
    }

    fun getAesKeySync(): String? {
        return getSync("user_id")
    }

    fun setAesKeySync(key: String) {
        setSync("user_id", key)
    }

    fun deleteAesKeySync() {
        deleteSync("user_id")
    }

    fun getDeviceIdSync(): String? {
        return getSync("device_id")
    }

    fun setDeviceIdSync(key: String) {
        setSync("device_id", key)
    }

    fun getJsBridgeKey(): String? {
        return getSync("js_bridge_key")
    }

    fun setJsBridgeKey(key: String) {
        setSync("js_bridge_key", key)
    }

    fun getJsBridgeSignKey(): String? {
        return getSync("js_bridge_sign_key")
    }

    fun setJsBridgeSignKey(key: String) {
        setSync("js_bridge_sign_key", key)
    }

    fun getJsBridgeKeyExpire(): Long? {
        return getSync("js_bridge_key_expire")?.toLongOrNull()
    }

    fun setJsBridgeKeyExpire(key: Long) {
        setSync("js_bridge_key_expire", key.toString())
    }


}