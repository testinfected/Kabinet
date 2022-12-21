package com.vtence.kabinet

import java.sql.PreparedStatement

class ParameterizedStatement(private val sql: String) :  SqlStatement, Preparable {
    private var autoGeneratedKeys: Boolean = false
    private val data: MutableMap<String, Any?> = mutableMapOf()

    fun retrieveGeneratedKeys() = apply { autoGeneratedKeys = true }

    operator fun set(name: String, value: Any?) {
        data[":$name"] = value
    }

    override fun build(statement: SqlBuilder) = statement {
        val (sql, args) = prepareSql()
        +sql.asExpression<Any?>(args)
    }

    private fun prepareSql(): Pair<String, List<Any?>> {
        val names = data.keys.sortedByDescending { it.length }

        val args = names.mapNotNull { name ->
            name.toRegex().find(sql)?.let { it.range.first to name}
        }.toMap()

        val statement = names.fold(sql) { sql, name ->
            sql.replace(name.toRegex()) { "?" }
        }

        val parameters = args.toSortedMap().values.map { data[it] }
        return statement to parameters
    }

    override fun <T> prepare(execute: (PreparedStatement) -> T): JdbcStatement<T> {
        return PreparedJdbcStatement(this, execute, autoGeneratedKeys)
    }
}


