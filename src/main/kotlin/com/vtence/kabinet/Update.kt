package com.vtence.kabinet


class Update(private val set: ColumnSet, private val values: DataChange) : Command {
    private val statement = UpdateStatement(set)
    private val parameters = mutableListOf<Any?>()

    override fun execute(executor: StatementExecutor): Int {
        return executor.execute(statement.compile(values.parameters + parameters) { update ->
            values(update)
            update.setParameters(parameters, offset = set.columns.size)
            update.executeUpdate()
        })
    }

    override fun toString(): String = statement.toSql()

    fun where(condition: String, vararg params: Any?): Command = where(predicate(condition), *params)

    fun where(condition: Expression, vararg params: Any?): Command {
        statement.where(condition)
        parameters.addAll(params)
        return this
    }

    companion object {
        fun set(
            columns: ColumnSet,
            values: DataChange
        ): Update {
            return Update(columns, values)
        }
    }
}


fun <T : Table> T.update(record: T.(Dataset) -> Unit): Update {
    val change = Dataset.opened().apply { record(this) }
    return Update.set(slice(change.columns), values = change)
}

fun <T : Table> T.updateWhere(condition: String, vararg params: Any?, record: T.(Dataset) -> Unit): Command =
    update(record).where(condition, *params)
