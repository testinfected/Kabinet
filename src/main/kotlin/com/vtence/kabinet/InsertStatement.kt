package com.vtence.kabinet

import java.sql.PreparedStatement

class InsertStatement(private val table: Table, private val values: Iterable<Any?>) : Expression, Preparable {

    private val columns = table.nonAutoGeneratedColumns()

    override fun build(statement: SqlBuilder) = statement {
        append("INSERT INTO ", table)
        columns.join(prefix = "(", postfix = ")") { +it.unqualified() }
        append(" VALUES")
        columns.zip(values).join(prefix = "(", postfix = ")") { (column, value) ->
            appendArgument(column.type to value)
        }
    }

    override fun <T> prepare(query: (PreparedStatement) -> T): JdbcStatement<T> {
        return PreparedJdbcStatement(this, query,true)
    }
}
