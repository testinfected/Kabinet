package com.vtence.kabinet

import java.sql.Connection


fun sql(sql: String): Script = Script(sql)

class Script(sql: String) {
    private val statement = SqlStatement(sql)

    operator fun set(name: String, value: Any?): Script {
        statement[name] = value
        return this
    }

    fun insert(connection: Connection): Int = insert(StatementExecutor(connection))

    fun insert(db: StatementExecutor): Int = insert(db, id)

    fun <T> insert(connection: Connection, handleKeys: KeyHandler<T>): T {
        return insert(StatementExecutor(connection), handleKeys)
    }

    fun <T> insert(db: StatementExecutor, handleKeys: KeyHandler<T>): T {
        return db.execute(statement.retrieveGeneratedKeys().compile {
            it.executeUpdate()
            it.generatedKeys.run {
                next()
                handleKeys(this)
            }
        })
    }

    fun <T> list(connection: Connection, hydrate: Hydrator<T>): List<T> =
        list(StatementExecutor(connection)) { hydrate(it) }

    fun <T> list(db: StatementExecutor, hydrate: Hydrator<T>): List<T> {
        return db.execute(statement.compile {
            val rs = it.executeQuery()
            val result = mutableListOf<T>()
            while (rs.next()) {
                result += hydrate(rs)
            }
            result.toList()
        })
    }

    override fun toString(): String = statement.toSql()
}
