package cc.typex.app.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// 替代方案（更适合区块链地址的场景）
fun String.shortAddress(
    prefixLength: Int = 5,
    suffixLength: Int = 3,
    separator: String = "…"
): String {
    // 如果字符串长度小于等于 prefixLength + suffixLength + 1，则直接返回原字符串
    if (this.length <= prefixLength + suffixLength + 1) {
        return this
    }

    // 取前prefixLength位和后suffixLength位，中间用separator连接
    return "${this.take(prefixLength)}$separator${this.takeLast(suffixLength)}"
}

private val formatterCache = mutableMapOf<String, DateTimeFormatter>()

fun String.toLocalTimestamp(format: String): String {
    val formatter = formatterCache[format]
        ?: DateTimeFormatter.ofPattern(format).also { formatterCache[format] = it }
    val timestamp = this.toLong()
    val instant = Instant.ofEpochMilli(timestamp)
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    return localDateTime.format(formatter)
}