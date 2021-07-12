package com.vtence.kabinet

import java.sql.Connection
import java.sql.ResultSet


class Script(sql: String) {
    private val statement = SqlStatement(sql)
    private val data: MutableMap<String, Any?> = mutableMapOf()

    operator fun set(name: String, value: Any?): Script = apply {
        data[":$name"] = value
    }

    fun <T> insert(executor: StatementExecutor, handleKeys: ResultSet.() -> T): T {
        return executor.execute(statement.retrieveGeneratedKeys().compile(parameters) {
            it.setParameters(parameters)
            it.executeUpdate()
            it.generatedKeys.run {
                next()
                handleKeys()
            }
        })
    }

    fun <T> list(executor: StatementExecutor, hydrate: ResultSet.() -> T): List<T> {
        return executor.execute(statement.compile(parameters) {
            it.setParameters(parameters)
            val rs = it.executeQuery()
            val result = mutableListOf<T>()
            while (rs.next()) {
                result += rs.hydrate()
            }
            result.toList()
        })
    }

    private val parameters get() = statement.parameters.map { data[it] }

    override fun toString(): String = statement.toSql()
}

fun Script.insert(connection: Connection): Unit = insert(StatementExecutor(connection))

fun Script.insert(executor: StatementExecutor): Unit = insert(executor) {}

fun <T> Script.insert(connection: Connection, handleKeys: ResultSet.() -> T): T =
    insert(StatementExecutor(connection)) { handleKeys() }

fun <T> Script.list(connection: Connection, hydrate: ResultSet.() -> T): List<T> =
    list(StatementExecutor(connection)) { hydrate() }


fun sql(sql: String): Script = Script(sql)

