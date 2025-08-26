package org.bigcraft.customitems.config

import me.wyne.wutils.common.Ticks
import org.bukkit.configuration.ConfigurationSection

interface Duration {
    fun getMillis(duration: Long): Long
}

object Seconds : Duration {
    override fun getMillis(duration: Long): Long = duration * 1000
}

object Minutes : Duration {
    override fun getMillis(duration: Long): Long = duration * 60 * 1000
}

object Hours : Duration {
    override fun getMillis(duration: Long): Long = duration * 60 * 60 * 1000
}

object Days : Duration {
    override fun getMillis(duration: Long): Long = duration * 24 * 60 * 60 * 1000
}

object Ticks : Duration {
    override fun getMillis(duration: Long): Long = Ticks.toMillis(duration)
}

fun getDuration(symbol: String): Duration {
    return when (symbol) {
        "s" -> Seconds
        "m" -> Minutes
        "h" -> Hours
        "d" -> Days
        "t" -> Ticks
        else -> Ticks
    }
}

fun ConfigurationSection.getMillis(path: String): Long {
    val (duration, durationType) = DURATION_SYMBOLS_REGEX.matchEntire(getString(path)!!)!!.destructured
    return getDuration(durationType).getMillis(duration.toLong())
}

val DURATION_SYMBOLS_REGEX = Regex("(-?\\d+(?:\\.\\d+)?)([smhdt])?")
