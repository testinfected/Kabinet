package com.vtence.kabinet

import java.sql.Connection
import java.sql.PreparedStatement


class Update(table: Table, private val columns: List<Column<*>>, private val values: DataChange) {
    private val statement = UpdateStatement(table.tableName, columns.names)
    private val parameters = mutableListOf<Any?>()

    fun execute(db: StatementExecutor): Int {
        return db.execute(statement.compile { update ->
            values.applyTo(update)
            setParameters(update)
            update.executeUpdate()
        })
    }

    override fun toString(): String = statement.toSql()

    fun where(clause: String, vararg params: Any?): Update {
        statement.where(clause)
        parameters.addAll(params)
        return this
    }

    private fun setParameters(statement: PreparedStatement) {
        for (i in parameters.indices) {
            statement[columns.size + i + 1] = parameters[i]
        }
    }

    companion object {
        fun set(
            table: Table,
            vararg columns: Column<*> = table.columns.toTypedArray(),
            values: DataChange
        ): Update {
            return Update(table, columns.toList(), values)
        }
    }
}

fun Update.execute(connection: Connection): Int = execute(StatementExecutor(connection))


fun <T : Table> T.update(record: T.(Dataset) -> Unit): Update {
    val change = Dataset.opened().apply { record(this) }
    return Update.set(this, columns = change.columns.toTypedArray(), values = change)
}
