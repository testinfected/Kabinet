package com.vtence.kabinet

class Delete(table: Table): Command {
    private val statement = DeleteStatement(table)

    override fun execute(executor: StatementExecutor): Int {
        return executor.execute(statement.prepare { delete ->
            delete.executeUpdate()
        })
    }

    fun where(expression: Expression<Boolean>): Command = apply {
        statement.where(expression)
    }

    override fun toString(): String = statement.toSql()

    companion object {
        fun from(table: Table): Delete = Delete(table)
    }
}


fun Table.deleteAll(executor: StatementExecutor) =
    Delete.from(this).execute(executor)

fun Table.deleteWhere(condition: String, vararg arguments: Any): Command =
    deleteWhere(condition.asExpression(*arguments))

fun Table.deleteWhere(expression: Expression<Boolean>):Command =
    Delete.from(this).where(expression)

