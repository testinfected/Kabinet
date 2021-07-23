package com.vtence.kabinet

import java.sql.PreparedStatement

class InsertStatement(private val into: Table, private val values: Iterable<Any?>) : Expression, Preparable {

    private val columns = into.nonAutoGeneratedColumns()

    override fun build(statement: SqlStatement) = statement {
        append("INSERT INTO ")
        +into
        columns.join(prefix = "(", postfix = ")") { +it.unqualified() }
        append(" VALUES")
        columns.zip(values).join(prefix = "(", postfix = ")") { (column, value) ->
            appendArgument(column.type to value)
        }
    }

    override fun <T> prepare(parameters: Iterable<Any?>, query: (PreparedStatement) -> T): JdbcStatement<T> {
        return PreparedJdbcStatement(this, query,true)
    }
}
