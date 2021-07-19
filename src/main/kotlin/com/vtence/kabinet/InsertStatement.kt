package com.vtence.kabinet

import java.sql.PreparedStatement
import java.sql.Statement

class InsertStatement(private val into: Table) : Compilable {

    private val columns = into.columns.notAutoGenerated()

    fun toSql(): String = buildSql {
        append("INSERT INTO ")
        +into
        columns.join(prefix = "(", postfix = ")") { +it.unqualified() }
        append(" VALUES")
        columns.join(prefix = "(", postfix = ")") { append("?") }
    }

    override fun <T> compile(parameters: Iterable<Any?>, query: (PreparedStatement) -> T): JdbcStatement<T> {
        return PreparedJdbcStatement(
            toSql(),
            parameters,
            { it.prepareStatement(toSql(), Statement.RETURN_GENERATED_KEYS) },
            query
        )
    }
}
