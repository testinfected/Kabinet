package com.vtence.kabinet


fun interface Expression {
    fun appendTo(sql: SqlBuilder)
}


class Literal(private val value: String): Expression {
    override fun appendTo(sql: SqlBuilder) {
        sql.append(value)
    }
}

fun lit(value: String) = Literal(value)


fun buildSql(body: SqlBuilder.() -> Unit): String {
    return SqlBuilder().apply(body).toString()
}

class SqlBuilder {

    private val sql = StringBuilder()

    fun append(value: String): SqlBuilder = apply { sql.append(value) }

    fun appendValue(value: Int): SqlBuilder = apply { sql.append(value) }

    fun append(expr: Expression): SqlBuilder = apply(expr::appendTo)

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

    operator fun Expression.unaryPlus(): SqlBuilder = append(this@unaryPlus)

    override fun toString(): String = sql.toString()
}

fun SqlBuilder.append(sql: SqlBuilder): SqlBuilder = append(sql.toString())

