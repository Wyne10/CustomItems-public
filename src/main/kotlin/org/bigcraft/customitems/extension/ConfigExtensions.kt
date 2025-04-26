package org.bigcraft.customitems.extension

import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.Vector

const val RANGE_DELIMITER = ".."
const val COMMA_DELIMITER = ","
const val COLON_DELIMITER = ":"
const val SPACE_DELIMITER = " "

val SINGLE_RANGE = 1..1
val ZERO_VECTOR = Vector()

fun ConfigurationSection.getIntRange(path: String, def: IntRange = SINGLE_RANGE): IntRange {
    val args = getString(path)?.split(RANGE_DELIMITER)
    args?.let {
        return IntRange(it.first().toInt(), it.last().toInt())
    }
    return def
}

fun ConfigurationSection.getPoint(path: String, def: Vector = ZERO_VECTOR): Vector {
    val args = getString(path)?.split(COMMA_DELIMITER)
    args?.let {
        return Vector(args[0].toInt(), args[1].toInt(), args[2].toInt())
    }
    return def
}

fun ConfigurationSection.getMaterial(path: String, def: Material = Material.STONE): Material =
    getString(path)?.let { Material.matchMaterial(it) } ?: def

fun ConfigurationSection.getBlockMaterial(path: String, def: Material = Material.STONE): Material =
    getMaterial(path, def)
        .also { if (!it.isBlock) throw IllegalArgumentException("Expected block at path ${"$currentPath.$path"}") }

fun ConfigurationSection.getIntNullable(path: String, def: Int? = null): Int? =
    if (contains(path)) getInt(path) else def