package com.vtence.kabinet

import java.sql.PreparedStatement


fun interface DataChange {
    fun applyTo(statement: PreparedStatement)
}


class Dataset(private val base: List<Column<*>>): DataChange, ColumnSet {
    private val values: MutableMap<Column<*>, Any?> = LinkedHashMap()

    operator fun <V> set(column: Column<V>, value: V?) {
        when {
            values.containsKey(column) -> error("$column already present in dataset")
            else -> values[column] = value
        }
    }

    override val columns: List<Column<*>>
        get() = base.ifEmpty { values.keys.toList() }

    override fun applyTo(statement: PreparedStatement) {
        columns.onEachIndexed { index, column ->
            column.set(statement, index + 1, values[column])
        }
    }

    companion object {
        fun closed(vararg columns: Column<*>) = closed(columns.toList())

        fun closed(columns: List<Column<*>>) = Dataset(columns)

        fun opened() = Dataset(listOf())
    }
}
