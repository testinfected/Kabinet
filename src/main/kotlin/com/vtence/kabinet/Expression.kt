package com.vtence.kabinet

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime


fun interface Expression<T> {
    fun build(statement: SqlBuilder)

    companion object {
        fun <T> build(build: SqlBuilder.() -> Unit): Expression<T> = Expression { it.build() }
    }
}

interface Field<T> : Expression<T> {
    val type: ColumnType<T>

    fun T.asParameter(): Expression<T> = toQueryParameter(this, type)
}


typealias Argument<T> = Pair<ColumnType<T>, T>


abstract class SqlBuilder(private val prepared: Boolean = false) {
    private val sql = StringBuilder()
    private val args = mutableListOf<Argument<*>>()

    val arguments: List<Argument<*>> get() = args

    operator fun invoke(body: SqlBuilder.() -> Unit): Unit = body()

    fun append(text: String): SqlBuilder = apply { sql.append(text) }

    fun appendValue(value: Any): SqlBuilder = apply { sql.append(value) }

    fun append(expr: Expression<*>): SqlBuilder = apply(expr::build)

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

    operator fun Expression<*>.unaryPlus(): SqlBuilder = append(this@unaryPlus)

    operator fun Argument<*>.unaryPlus(): SqlBuilder = appendArgument(this@unaryPlus)

    fun <T> T.toArgument(type: ColumnType<T>): Expression<T> = toQueryParameter(this, type)

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
            is Expression<*> -> append(value)
            else -> appendValue(value)
        }
    }
}


class SqlStatementBuilder(prepared: Boolean) : SqlBuilder(prepared)

fun buildStatement(prepared: Boolean = false, body: SqlBuilder.() -> Unit): SqlBuilder {
    return SqlStatementBuilder(prepared).apply(body)
}


@Suppress("UNCHECKED_CAST")
internal fun <T> toQueryParameter(value: T, type: ColumnType<T>): Expression<T> = when (value) {
    is Boolean -> booleanParam(value)
    is Int -> intParam(value)
    is Long -> longParam(value)
    is BigDecimal -> decimalParam(value)
    is Instant -> instantParam(value)
    is LocalDate -> dateParam(value)
    is LocalTime -> timeParam(value)
    is String -> QueryParameter(value, type) // string values use column type
    else -> QueryParameter(value, type) // for null values and other types
} as Expression<T>


private fun booleanParam(value: Boolean): Expression<Boolean> = QueryParameter(value, BooleanColumnType)

private fun intParam(value: Int): Expression<Int> = QueryParameter(value, IntColumnType)

private fun longParam(value: Long): Expression<Long> = QueryParameter(value, LongColumnType)

private fun decimalParam(value: BigDecimal): Expression<BigDecimal> =
    QueryParameter(value, DecimalColumnType(value.precision(), value.scale()))

private fun instantParam(value: Instant): Expression<Instant> = QueryParameter(value, InstantColumnType)

private fun dateParam(value: LocalDate): Expression<LocalDate> = QueryParameter(value, LocalDateColumnType)

private fun timeParam(value: LocalTime): Expression<LocalTime> = QueryParameter(value, LocalTimeColumnType)

