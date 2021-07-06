package com.vtence.kabinet

import java.sql.ResultSet


class KeySet(private val rows: List<ResultRow>) {
    infix operator fun <T> get(column: Column<T>): T {
        val row = rows.firstOrNull() ?: error("No key generated")
        return row[column]
    }

    companion object {
        fun none(): KeySet = KeySet(listOf())
    }
}


class ResultRow(private val fields: Map<Column<*>, Int>) {
    private val data = arrayOfNulls<Any?>(fields.size)

    operator fun <T> get(column: Column<T>): T {
        val index = fields[column] ?: error("'$column' not in result set")
        val value = data[index]
        if (value == null && !column.nullable) error("'$column' is null in result set")
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    companion object {
        fun readFrom(rs: ResultSet, columns: List<Column<*>>): ResultRow {
            val fields = columns.mapIndexed { index, col -> col to index }.toMap()

            return ResultRow(fields).apply {
                fields.forEach { (field, index) ->
                    val value = field.get(rs, index + 1)
                    data[index] = value
                }
            }
        }
    }
}


fun ResultSet.read(columns: List<Column<*>>): List<ResultRow> {
    val results = mutableListOf<ResultRow>()
    while (next()) {
        results += ResultRow.readFrom(this, columns = columns)
    }
    return results.toList()
}

