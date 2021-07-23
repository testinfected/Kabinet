package com.vtence.kabinet

import java.sql.Connection
import java.sql.ResultSet


class PlainSql(sql: String) {
    private val statement = ParametrizedStatement(sql)

    operator fun set(name: String, value: Any?): PlainSql = apply {
        statement[name] = value
    }

    fun <T> insert(executor: StatementExecutor, handleKeys: ResultSet.() -> T): T {
        return executor.execute(statement.retrieveGeneratedKeys().prepare {
            it.executeUpdate()
            it.generatedKeys.run {
                next()
                handleKeys()
            }
        })
    }

    fun <T> list(executor: StatementExecutor, hydrate: ResultSet.() -> T): List<T> {
        return executor.execute(statement.prepare {
            val rs = it.executeQuery()
            val result = mutableListOf<T>()
            while (rs.next()) {
                result += rs.hydrate()
            }
            result.toList()
        })
    }
}

fun PlainSql.insert(connection: Connection): Unit = insert(StatementExecutor(connection))

fun PlainSql.insert(executor: StatementExecutor): Unit = insert(executor) {}

fun <T> PlainSql.insert(connection: Connection, handleKeys: ResultSet.() -> T): T =
    insert(StatementExecutor(connection)) { handleKeys() }

fun <T> PlainSql.list(connection: Connection, hydrate: ResultSet.() -> T): List<T> =
    list(StatementExecutor(connection)) { hydrate() }


fun sql(statement: String): PlainSql = PlainSql(statement)

