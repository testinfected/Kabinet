package com.vtence.kabinet


fun buildSql(body: SqlBuilder.() -> Unit): String {
    return SqlBuilder().apply(body).toString()
}

class SqlBuilder {

    private val sql = StringBuilder()

    fun append(value: String): SqlBuilder = apply { sql.append(value) }

    fun appendValue(value: Int): SqlBuilder = apply { sql.append(value) }

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

    fun isNotEmpty(): Boolean = sql.isNotEmpty()

    override fun toString(): String = sql.toString()
}


fun SqlBuilder.append(sql: SqlBuilder): SqlBuilder = append(sql.toString())

