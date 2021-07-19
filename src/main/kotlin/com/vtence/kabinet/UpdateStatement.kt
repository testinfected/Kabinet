package com.vtence.kabinet

import java.sql.PreparedStatement

class UpdateStatement(private val set: ColumnSet) : Compilable {
    private var whereClause: Expression? = null

    fun where(clause: Expression) {
        whereClause = clause
    }

    fun toSql(): String = buildSql {
        append("UPDATE ")
        +set
        append(" SET ")
        set.columns.join {
            +it.unqualified()
            append(" = ?")
        }
        whereClause?.let {
            append(" WHERE ")
            +it
        }
    }

    override fun <T> compile(parameters: Iterable<Any?>, query: (PreparedStatement) -> T): JdbcStatement<T> {
        return PreparedJdbcStatement(toSql(), parameters, { it.prepareStatement(toSql()) }, query)
    }

    override fun toString(): String = toSql()
}
