package com.vtence.kabinet


interface DataChange {
    val values: Iterable<Any?>
}


class Dataset(private val base: List<Column<*>>): DataChange {
    private val data: MutableMap<Column<*>, Any?> = LinkedHashMap()

    operator fun <V> set(column: Column<V>, value: V) {
        when {
            this.data.containsKey(column) -> error("'$column' already present in dataset")
            else -> this.data[column] = value
        }
    }

    override val values: List<Any?>
        get() = columns.map { this.data[it] }

    val columns: List<Column<*>>
        get() = base.ifEmpty { this.data.keys.toList() }

    companion object {
        fun closed(columns: List<Column<*>>) = Dataset(columns)

        fun opened() = Dataset(listOf())
    }
}

typealias Dehydrator<T> = T.(Dataset) -> Unit