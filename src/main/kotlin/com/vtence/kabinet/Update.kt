package com.vtence.kabinet


class Update(set: ColumnSet, data: DataChange) : Command {
    private val statement = UpdateStatement(set, data.values)

    fun where(condition: Expression): Command = apply {
        statement.where(condition)
    }

    override fun execute(executor: StatementExecutor): Int {
        return executor.execute(statement.prepare { update ->
            update.executeUpdate()
        })
    }

    override fun toString(): String = statement.toSql()

    companion object {
        fun set(columns: ColumnSet, values: DataChange): Update = Update(columns, values)
    }
}


fun <T : Table> T.update(record: T.(Dataset) -> Unit): Update {
    val change = Dataset.opened().apply { record(this) }
    return Update.set(slice(change.columns), values = change)
}

fun <T : Table> T.updateWhere(condition: String, vararg params: Any?, record: T.(Dataset) -> Unit): Command =
    updateWhere(PreparedExpression(condition, params.toList()), record)

fun <T : Table> T.updateWhere(expression: Expression, record: T.(Dataset) -> Unit): Command =
    update(record).where(expression)
