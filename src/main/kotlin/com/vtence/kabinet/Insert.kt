package com.vtence.kabinet

import java.sql.Connection


class Insert(table: Table, private val values: Dehydrator) {
    val statement = InsertStatement(table.tableName, table.columnNames(false))

    fun execute(connection: Connection): Int = execute(StatementExecutor(connection))

    fun execute(db: StatementExecutor): Int = execute(db, id)

    fun <T> execute(connection: Connection, handleKeys: KeyHandler<T>): T =
        execute(StatementExecutor(connection), handleKeys)

    fun <T> execute(db: StatementExecutor, handleKeys: KeyHandler<T>): T {
        return db.execute(statement.compile { insert ->
            values(insert)
            insert.executeUpdate()
            val keys = insert.generatedKeys.also { it.next() }
            handleKeys(keys)
        })
    }

    override fun toString(): String {
        return statement.toSql()
    }

    companion object {
        fun into(table: Table, values: Dehydrator): Insert {
            return Insert(table, values)
        }
    }
}


fun Table.insert(values: Dehydrator) = Insert.into(this, values)
