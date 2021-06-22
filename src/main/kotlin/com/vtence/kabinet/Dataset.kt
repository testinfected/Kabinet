package com.vtence.kabinet

import java.sql.PreparedStatement


class Dataset(private val table: ColumnSet) {
    private val values: MutableMap<Column<*>, Any?> = mutableMapOf()

    operator fun <V> set(column: Column<V>, value: V) {
        when {
            values.containsKey(column) -> error("$column already present in dataset")
            else -> values[column] = value
        }
    }

    fun fill(statement: PreparedStatement) {
        table.columns(false).onEachIndexed { index, column ->
            column.set(statement, index + 1, values[column])
        }
    }
}
