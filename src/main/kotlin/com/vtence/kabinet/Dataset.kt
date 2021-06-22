package com.vtence.kabinet

import java.sql.PreparedStatement


fun interface DataChange {
    fun applyTo(statement: PreparedStatement)
}


class Dataset(private val table: ColumnSet): DataChange {
    private val values: MutableMap<Column<*>, Any?> = mutableMapOf()

    operator fun <V> set(column: Column<V>, value: V) {
        when {
            values.containsKey(column) -> error("$column already present in dataset")
            else -> values[column] = value
        }
    }

    override fun applyTo(statement: PreparedStatement) {
        table.columns(false).onEachIndexed { index, column ->
            column.set(statement, index + 1, values[column])
        }
    }
}
