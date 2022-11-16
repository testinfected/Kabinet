package com.vtence.kabinet

import java.sql.Connection
import java.sql.ResultSet


class PlainSql(sql: String) {
    private val statement = ParameterizedStatement(sql)

    operator fun set(name: String, value: Any?): PlainSql = apply {
        statement[name] = value
    }

    fun <T> insert(executor: StatementExecutor, handleKeys: (ResultSet) -> T): T {
        return executor.execute(statement.retrieveGeneratedKeys().prepare {
            it.executeUpdate()
            it.generatedKeys.run {
                next()
                handleKeys(this)
            }
        })
    }

    fun <T> list(executor: StatementExecutor, hydrate: (ResultSet) -> T): List<T> {
        return executor.execute(statement.prepare {
            val rs = it.executeQuery()
            val result = mutableListOf<T>()
            while (rs.next()) {
                result += hydrate(rs)
            }
            result.toList()
        })
    }

    fun execute(executor: StatementExecutor): Int {
        return executor.execute(statement.retrieveGeneratedKeys().prepare {
            it.executeUpdate()
        })
    }

    override fun toString(): String {
        return super.toString()
    }
}

fun PlainSql.insert(connection: Connection): Unit = insert(StatementExecutor(connection))

fun PlainSql.insert(executor: StatementExecutor): Unit = insert(executor) {}

fun <T> PlainSql.insert(connection: Connection, handleKeys: (ResultSet) -> T): T =
    insert(StatementExecutor(connection)) { handleKeys(it) }

fun <T> PlainSql.list(connection: Connection, hydrate: (ResultSet) -> T): List<T> =
    list(StatementExecutor(connection)) { hydrate(it) }

fun <T> PlainSql.firstOrNull(connection: Connection, hydrate: (ResultSet) -> T): T? =
    firstOrNull(StatementExecutor(connection), hydrate)

fun <T> PlainSql.firstOrNull(executor: StatementExecutor, hydrate: (ResultSet) -> T): T? =
    list(executor, hydrate).firstOrNull()


fun sql(statement: String): PlainSql = PlainSql(statement)

