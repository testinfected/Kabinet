package com.vtence.kabinet

import java.sql.PreparedStatement


interface DataChange: (PreparedStatement) -> Unit {
    val parameters: Iterable<Any?>
}


class Dataset(private val base: List<Column<*>>): DataChange, ColumnSet {
    private val values: MutableMap<Column<*>, Any?> = LinkedHashMap()

    operator fun <V> set(column: Column<V>, value: V) {
        when {
            values.containsKey(column) -> error("'$column' already present in dataset")
            else -> values[column] = value
        }
    }

    override val parameters: List<Any?>
        get() = columns.map { values[it] }

    override val columns: List<Column<*>>
        get() = base.ifEmpty { values.keys.toList() }

    override operator fun invoke(statement: PreparedStatement) {
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
