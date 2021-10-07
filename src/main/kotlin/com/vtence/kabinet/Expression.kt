package com.vtence.kabinet

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate


fun interface Expression {
    fun build(statement: SqlStatement)

    companion object {
        fun build(builder: SqlStatement.() -> Unit) = Expression { builder(it) }
    }
}


typealias Argument<T> = Pair<ColumnType<T>, T?>

class SqlStatement(private val prepared: Boolean = false) {
    private val sql = StringBuilder()
    private val args = mutableListOf<Argument<*>>()

    val arguments: List<Argument<*>> get() = args

    operator fun invoke(body: SqlStatement.() -> Unit): Unit = body()

    fun append(text: String): SqlStatement = apply { sql.append(text) }

    fun appendValue(value: Any): SqlStatement = apply { sql.append(value) }

    fun append(expr: Expression): SqlStatement = apply(expr::build)

    fun appendArgument(argument: Argument<*>): SqlStatement = appendArgument(argument.first, argument.second)

    fun appendArgument(type: ColumnType<*>, value: Any?): SqlStatement = apply {
        if (prepared) append("?") else append(type.toSql(value))
        args.add(type to value)
    }

    fun <T> Iterable<T>.join(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        transform: SqlStatement.(T) -> Unit
    ) {
        sql.append(prefix)
        forEachIndexed { index, element ->
            if (index > 0) sql.append(separator)
            transform(element)
        }
        sql.append(postfix)
    }

    operator fun String.unaryPlus(): SqlStatement = append(this)

    operator fun Expression.unaryPlus(): SqlStatement = append(this@unaryPlus)

    operator fun Argument<*>.unaryPlus(): SqlStatement = appendArgument(this@unaryPlus)

    fun Any?.asArgument(): Expression = asParameterExpression(this)

    fun asSql(): String {
        return sql.toString()
    }

    override fun toString(): String = asSql()
}


private fun asParameterExpression(value: Any?): Expression = when (value) {
    is Boolean -> QueryParameter(value, BooleanColumnType)
    is Int -> QueryParameter(value, IntColumnType)
    is String -> QueryParameter(value, StringColumnType)
    is BigDecimal -> QueryParameter(value, DecimalColumnType(value.precision(), value.scale()))
    is Instant -> QueryParameter(value, InstantColumnType)
    is LocalDate -> QueryParameter(value, LocalDateColumnType)
    else -> QueryParameter(value, ObjectColumnType)
}


fun buildStatement(prepared: Boolean = false, body: SqlStatement.() -> Unit): SqlStatement {
    return SqlStatement(prepared).apply(body)
}

fun buildSql(prepared: Boolean = false, body: SqlStatement.() -> Unit): String {
    return buildStatement(prepared, body).asSql()
}

fun Expression.toSql(prepared: Boolean = false) = buildSql(prepared) { +this@toSql }

fun Expression.arguments(): List<Argument<*>> = SqlStatement(true).append(this).arguments

