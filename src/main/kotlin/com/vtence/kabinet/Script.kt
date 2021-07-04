package com.vtence.kabinet

import java.sql.Connection
import java.sql.ResultSet


class Script(sql: String) {
    private val statement = SqlStatement(sql)

    operator fun set(name: String, value: Any?): Script {
        statement[name] = value
        return this
    }

    fun <T> insert(executor: StatementExecutor, handleKeys: ResultSet.() -> T): T {
        return executor.execute(statement.retrieveGeneratedKeys().compile {
            it.executeUpdate()
            it.generatedKeys.run {
                next()
                handleKeys()
            }
        })
    }

    fun <T> list(executor: StatementExecutor, hydrate: ResultSet.() -> T): List<T> {
        return executor.execute(statement.compile {
            val rs = it.executeQuery()
            val result = mutableListOf<T>()
            while (rs.next()) {
                result += rs.hydrate()
            }
            result.toList()
        })
    }

    override fun toString(): String = statement.toSql()
}

fun Script.insert(connection: Connection): Unit = insert(StatementExecutor(connection))

fun Script.insert(executor: StatementExecutor): Unit = insert(executor) {}

fun <T> Script.insert(connection: Connection, handleKeys: ResultSet.() -> T): T =
    insert(StatementExecutor(connection)) { handleKeys() }

fun <T> Script.list(connection: Connection, hydrate: ResultSet.() -> T): List<T> =
    list(StatementExecutor(connection)) { hydrate() }


fun sql(sql: String): Script = Script(sql)

