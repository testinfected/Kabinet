package com.vtence.kabinet

class Delete(private val table: Table): Command {
    private val statement = DeleteStatement(table)

    override fun execute(executor: StatementExecutor): Int {
        return executor.execute(statement.prepare { delete ->
            delete.executeUpdate()
        })
    }

    fun where(expression: Expression): Command = apply {
        statement.where(expression)
    }

    companion object {
        fun from(table: Table): Delete = Delete(table)
    }
}


fun Table.deleteAll(executor: StatementExecutor) =
    Delete.from(this).execute(executor)

fun Table.deleteWhere(condition: String, vararg parameters: Any?): Command =
    deleteWhere(PreparedExpression(condition, parameters.toList()))

fun Table.deleteWhere(expression: Expression):Command =
    Delete.from(this).where(expression)

