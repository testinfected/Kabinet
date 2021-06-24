package com.vtence.kabinet

import java.sql.PreparedStatement
import java.sql.Statement

class InsertStatement(private val tableName: String, private val columnNames: List<String>): Compilable {

    fun toSql(): String {
        return buildString {
            append("INSERT INTO ").append(tableName)
            append("(").append(columnNames.joinToString()).append(")")
            append(" VALUES(").append(columnNames.joinToString { "?" }).append(")")
        }
    }

    override fun <T> compile(query: (PreparedStatement) -> T): JdbcStatement<T> {
        return JdbcStatement(toSql()) {
            it.prepareStatement(toSql(), Statement.RETURN_GENERATED_KEYS).use(query)
        }
    }
}
