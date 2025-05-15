package cc.typex.app.util

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowManager
import cc.typex.app.db.EntityDao
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt


@Singleton
class DeviceInfo @Inject constructor(
    @ApplicationContext private val context: Context,
    private val entityDao: EntityDao,
) {

    var navigationBarHeight: Int = 0
        private set
    var statusBarHeight: Int = 0
        private set

    val screenWidth: Int
    val screenHeight: Int
    val screenWidthDp: Int
    val screenHeightDp: Int

    init {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        windowManager.defaultDisplay.getRealSize(point)
        screenWidth = point.x
        screenHeight = point.y
        val dm = context.resources.displayMetrics
        screenWidthDp = (screenWidth / dm.density).roundToInt()
        screenHeightDp = (screenHeight / dm.density).roundToInt()
    }

    val platform = "and"
    val appVersion: String
    val osVersion = Build.VERSION.SDK_INT
    val idfa: String
    val brand = Build.BRAND
    val net = "WiFi"
    val timezone: String // Asia/Shanghai
    val lang = Locale.getDefault().toLanguageTag() // en-US
    val country = Locale.getDefault().displayCountry // United States
    val isDST: Boolean
    val deviceId: String

    init {
        val pkg = context.packageManager.getPackageInfo(context.packageName, 0)
        this.appVersion = pkg.versionName ?: ""

        this.idfa = ""

        val timeZone = TimeZone.getDefault()
        this.timezone = timeZone.id

        val calendar = Calendar.getInstance(timeZone)
        val offsetInMillis: Int = timeZone.getOffset(calendar.timeInMillis)
        this.isDST = timeZone.inDaylightTime(calendar.time) && offsetInMillis > 0
        this.deviceId = entityDao.getDeviceIdSync()
            ?: UUID.randomUUID().toString().also { entityDao.setDeviceIdSync(it) }
    }

    fun updateSystemBarHeight(statusBar: Int, bottomNavigation: Int) {
        statusBarHeight = statusBar
        navigationBarHeight = bottomNavigation
    }

    fun getDefaultAesKey(): SecretKeySpec {
        return SecretKeySpec(getDefaultAesKeyStr().toByteArray(), "AES")
    }

    fun getDefaultAesKeyStr(): String {
        val md = MessageDigest.getInstance("MD5")
        md.reset()
        return buildHexString(md.digest(deviceId.toByteArray()))
    }

    private fun buildHexString(data: ByteArray): String {
        val sb = StringBuilder()
        for (b in data) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }
}