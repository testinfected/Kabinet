package com.vtence.kabinet


class Update(table: Table, private val columns: List<Column<*>>, private val values: DataChange) : Command {
    private val statement = UpdateStatement(table.tableName, columns.names)
    private val parameters = mutableListOf<Any?>()

    override fun execute(executor: StatementExecutor): Int {
        return executor.execute(statement.compile(values.parameters + parameters) { update ->
            values(update)
            update.setParameters(parameters, offset = columns.size)
            update.executeUpdate()
        })
    }

    override fun toString(): String = statement.toSql()

    fun where(condition: String, vararg params: Any?): Command {
        statement.where(condition)
        parameters.addAll(params)
        return this
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


fun <T : Table> T.update(record: T.(Dataset) -> Unit): Update {
    val change = Dataset.opened().apply { record(this) }
    return Update.set(this, columns = change.columns.toTypedArray(), values = change)
}

fun <T : Table> T.updateWhere(condition: String, vararg params: Any?, record: T.(Dataset) -> Unit): Command =
    update(record).where(condition, *params)
