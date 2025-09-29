package org.bigcraft.customitems.config

import org.bukkit.configuration.ConfigurationSection
import kotlin.math.pow

//region Type operations

interface Operations<T : Number> {
    fun add(first: T, second: T): T
    fun subtract(first: T, second: T): T
    fun multiply(first: T, second: T): T
    fun divide(first: T, second: T): T
    fun power(first: T, second: T): T
}

object IntOperations : Operations<Int> {
    override fun add(first: Int, second: Int): Int = first + second
    override fun subtract(first: Int, second: Int): Int = first - second
    override fun multiply(first: Int, second: Int): Int = first * second
    override fun divide(first: Int, second: Int): Int = first / second
    override fun power(first: Int, second: Int): Int = first.toDouble().pow(second.toDouble()).toInt()
}

object DoubleOperations : Operations<Double> {
    override fun add(first: Double, second: Double): Double = first + second
    override fun subtract(first: Double, second: Double): Double = first - second
    override fun multiply(first: Double, second: Double): Double = first * second
    override fun divide(first: Double, second: Double): Double = first / second
    override fun power(first: Double, second: Double): Double = first.pow(second)
}

fun <T : Number> Number.operations(): Operations<T> =
    when(this) {
        is Int -> IntOperations
        is Double -> DoubleOperations
        else -> throw IllegalArgumentException("Unknown operations type")
    } as Operations<T>
val Int.Operations: Operations<Int>
    get() = IntOperations
val Double.Operations: Operations<Double>
    get() = DoubleOperations

//endregion

//region Operations

interface Operation<T : Number> {
    fun evaluate(first: T, second: T): T
}

interface ContainedOperation<T : Number> {
    fun evaluate(value: T): T
}

class IntOperation(private val second: Int, private val operation: Operation<Int>) : ContainedOperation<Int> {
    override fun evaluate(value: Int): Int =
        operation.evaluate(value, second)
}

class DoubleOperation(private val second: Double, private val operation: Operation<Double>) : ContainedOperation<Double> {
    override fun evaluate(value: Double): Double =
        operation.evaluate(value, second)
}

class Plus<T : Number> : Operation<T> {
    override fun evaluate(first: T, second: T): T =
        first.operations<T>().add(first, second)
}

class Minus<T : Number> : Operation<T> {
    override fun evaluate(first: T, second: T): T =
        first.operations<T>().subtract(first, second)
}

class Multiply<T : Number> : Operation<T> {
    override fun evaluate(first: T, second: T): T =
        first.operations<T>().multiply(first, second)
}

class Divide<T : Number> : Operation<T> {
    override fun evaluate(first: T, second: T): T =
        first.operations<T>().divide(first, second)
}

class Power<T : Number> : Operation<T> {
    override fun evaluate(first: T, second: T): T =
        first.operations<T>().power(first, second)
}

class Set<T : Number> : Operation<T> {
    override fun evaluate(first: T, second: T): T =
        second
}

fun <T : Number> getOperation(symbol: String): Operation<T> {
    return when (symbol) {
        "+" -> Plus()
        "-" -> Minus()
        "*" -> Multiply()
        "/" -> Divide()
        "**" -> Power()
        else -> Set()
    }
}

fun ConfigurationSection.getIntOperation(path: String): IntOperation {
    val (operation, number) = OPERATION_SYMBOLS_REGEX.matchEntire(getString(path)!!)!!.destructured
    return IntOperation(number.toInt(), getOperation(operation))
}

fun ConfigurationSection.getDoubleOperation(path: String): DoubleOperation {
    val (operation, number) = OPERATION_SYMBOLS_REGEX.matchEntire(getString(path)!!)!!.destructured
    return DoubleOperation(number.toDouble(), getOperation(operation))
}

val OPERATION_SYMBOLS_REGEX = Regex("(\\+|-|\\*|/|\\*\\*)?(-?\\d+(?:\\.\\d+)?)")

//endregion