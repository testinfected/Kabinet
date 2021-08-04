package com.vtence.kabinet


class Update(set: ColumnSet, values: DataChange) : Command {
    private val statement = UpdateStatement(set, values.values)

    fun where(condition: String, vararg params: Any?): Command =
        where(PreparedExpression(condition, params.toList()))

    fun where(condition: Expression): Command {
        statement.where(condition)
        return this
    }

    override fun execute(executor: StatementExecutor): Int {
        return executor.execute(statement.prepare { update ->
            update.executeUpdate()
        })
    }

    override fun toString(): String = statement.asSql()

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
