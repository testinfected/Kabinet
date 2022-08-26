package com.vtence.kabinet


interface DataChange {
    val values: Iterable<Any?>
}


class DataSet(private val base: List<Column<*>>): DataChange {
    private val data: MutableMap<Column<*>, Any?> = LinkedHashMap()

    val columns: List<Column<*>>
        get() = base.ifEmpty { this.data.keys.toList() }

    override val values: List<Any?>
        get() = columns.map { this.data[it] }

    operator fun <V> set(column: Column<V>, value: V) {
        when {
            this.data.containsKey(column) -> error("'$column' already present in dataset")
            else -> this.data[column] = value
        }
    }

    companion object {
        fun closed(columns: List<Column<*>>) = DataSet(columns)

        fun opened() = DataSet(listOf())
    }
}

typealias Dehydrator<T> = T.(DataSet) -> Unit