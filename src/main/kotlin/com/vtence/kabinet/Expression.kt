package com.vtence.kabinet

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime


fun interface Expression {
    fun build(statement: SqlBuilder)

    fun Any.asArgument() = toQueryParameter(this)

    companion object {
        fun build(builder: SqlBuilder.() -> Unit) = Expression { builder(it) }
    }
}


typealias Argument<T> = Pair<ColumnType<T>, T?>


abstract class SqlBuilder(private val prepared: Boolean = false) {
    private val sql = StringBuilder()
    private val args = mutableListOf<Argument<*>>()

    val arguments: List<Argument<*>> get() = args

    operator fun invoke(body: SqlBuilder.() -> Unit): Unit = body()

    fun append(text: String): SqlBuilder = apply { sql.append(text) }

    fun appendValue(value: Any): SqlBuilder = apply { sql.append(value) }

    fun append(expr: Expression): SqlBuilder = apply(expr::build)

    fun appendArgument(argument: Argument<*>): SqlBuilder = appendArgument(argument.first, argument.second)

    fun appendArgument(type: ColumnType<*>, value: Any?): SqlBuilder = apply {
        if (prepared) append("?") else append(type.toSql(value))
        args.add(type to value)
    }

    fun <T> Iterable<T>.join(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        transform: SqlBuilder.(T) -> Unit
    ) {
        sql.append(prefix)
        forEachIndexed { index, element ->
            if (index > 0) sql.append(separator)
            transform(element)
        }
        sql.append(postfix)
    }

    operator fun String.unaryPlus(): SqlBuilder = append(this)

    operator fun Expression.unaryPlus(): SqlBuilder = append(this@unaryPlus)

    operator fun Argument<*>.unaryPlus(): SqlBuilder = appendArgument(this@unaryPlus)

    fun Any.asArgument(): Expression = toQueryParameter(this)

    fun asSql(): String {
        return sql.toString()
    }

    override fun toString(): String = asSql()
}


fun SqlBuilder.append(vararg expressions: Any): SqlBuilder = apply {
    for (value in expressions) {
        when (value) {
            is Char -> append(value.toString())
            is String -> append(value)
            is Expression -> append(value)
            else -> appendValue(value)
        }
    }
}


class SqlStatement(prepared: Boolean) : SqlBuilder(prepared) {
    companion object {
        fun prepared(build: SqlBuilder.() -> Unit): SqlBuilder = SqlStatement(prepared = true).apply {
            this.build()
        }

        fun plain(): SqlBuilder = SqlStatement(prepared = false)
    }
}


private fun toQueryParameter(value: Any): Expression = when (value) {
    is Boolean -> booleanParam(value)
    is Int -> intParam(value)
    is String -> stringParam(value)
    is BigDecimal -> decimalParam(value)
    is Instant -> instantParam(value)
    is LocalDate -> dateParam(value)
    is LocalTime -> timeParam(value)
    else -> objectParam(value)
}

fun booleanParam(value: Boolean): Expression = QueryParameter(value, BooleanColumnType)

fun intParam(value: Int): Expression = QueryParameter(value, IntColumnType)

fun stringParam(value: String): Expression = QueryParameter(value, StringColumnType)

fun decimalParam(value: BigDecimal): Expression =
    QueryParameter(value, DecimalColumnType(value.precision(), value.scale()))

fun instantParam(value: Instant): Expression = QueryParameter(value, InstantColumnType)

fun dateParam(value: LocalDate): Expression = QueryParameter(value, LocalDateColumnType)

fun timeParam(value: LocalTime): Expression = QueryParameter(value, LocalTimeColumnType)

fun objectParam(value: Any): Expression = QueryParameter(value, ObjectColumnType)


fun buildStatement(prepared: Boolean = false, body: SqlBuilder.() -> Unit): SqlBuilder {
    return SqlStatement(prepared).apply(body)
}

fun buildSql(prepared: Boolean = false, body: SqlBuilder.() -> Unit): String {
    return buildStatement(prepared, body).asSql()
}

fun Expression.toSql(prepared: Boolean = false) = buildSql(prepared) { +this@toSql }

fun Expression.arguments(): List<Argument<*>> = SqlStatement.prepared { +this@arguments }.arguments

