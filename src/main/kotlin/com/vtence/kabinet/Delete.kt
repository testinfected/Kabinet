package com.vtence.kabinet

class Delete(private val table: Table): Command {
    private val statement = DeleteStatement(table)

    override fun execute(executor: StatementExecutor): Int {
        return executor.execute(statement.prepare { delete ->
            delete.executeUpdate()
        })
    }

    companion object {
        fun from(table: Table): Delete = Delete(table)
    }
}


fun Table.deleteAll(executor: StatementExecutor) =
    Delete.from(this).execute(executor)

