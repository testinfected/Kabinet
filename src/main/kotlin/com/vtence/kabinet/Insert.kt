package com.vtence.kabinet

import java.sql.Connection


class Insert(private val table: Table, data: DataChange) {

    private val statement = InsertStatement(table, data.values)

    fun execute(executor: StatementExecutor): KeySet {
        return executor.execute(statement.prepare { insert ->
            val inserted = insert.executeUpdate()
            if (inserted > 0)
                KeySet(insert.generatedKeys.read(table.autoGeneratedColumns()))
            else
                KeySet.none()
        })
    }

    override fun toString(): String = statement.toSql()

    companion object {
        fun into(table: Table, values: DataChange): Insert {
            return Insert(table, values)
        }
    }
}

fun Insert.execute(connection: Connection): KeySet = execute(StatementExecutor(connection))


fun <T : Table> T.insert(record: Dehydrator<T>): Insert =
    Insert.into(this, DataSet.closed(nonAutoGeneratedColumns()).apply { record(this) })
