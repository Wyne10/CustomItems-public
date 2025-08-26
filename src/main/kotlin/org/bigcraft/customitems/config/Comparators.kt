package org.bigcraft.customitems.config

import org.bukkit.configuration.ConfigurationSection

interface Comparator<T> {
    fun compare(first: Comparable<T>, second: T): Boolean
}

interface ContainedComparator<T> {
    fun compare(value: T): Boolean
}

class IntComparator(private val second: Int, private val comparator: Comparator<Int>) : ContainedComparator<Int> {
    override fun compare(value: Int): Boolean =
        comparator.compare(value, second)
}

class LessThan<T> : Comparator<T> {
    override fun compare(first: Comparable<T>, second: T): Boolean {
        return first < second
    }
}

class GreaterThan<T> : Comparator<T> {
    override fun compare(first: Comparable<T>, second: T): Boolean {
        return first > second
    }
}

class LessOrEqual<T> : Comparator<T> {
    override fun compare(first: Comparable<T>, second: T): Boolean {
        return first <= second
    }
}

class GreaterOrEqual<T> : Comparator<T> {
    override fun compare(first: Comparable<T>, second: T): Boolean {
        return first >= second
    }
}

class Equals<T> : Comparator<T> {
    override fun compare(first: Comparable<T>, second: T): Boolean {
        return first == second
    }
}

fun <T> getComparator(symbol: String): Comparator<T> {
    return when (symbol) {
        "<" -> LessThan()
        ">" -> GreaterThan()
        "<=" -> LessOrEqual()
        ">=" -> GreaterOrEqual()
        "==" -> Equals()
        else -> Equals<T>()
    }
}

fun ConfigurationSection.getIntComparator(path: String): IntComparator {
    val (operator, number) = COMPARATOR_SYMBOLS_REGEX.matchEntire(getString(path)!!)!!.destructured
    return IntComparator(number.toInt(), getComparator(operator))
}

val COMPARATOR_SYMBOLS_REGEX = Regex("(<=|>=|==|<|>)?(-?\\d+(?:\\.\\d+)?)")