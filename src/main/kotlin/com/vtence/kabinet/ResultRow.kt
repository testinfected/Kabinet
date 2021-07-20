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


class ResultRow(private val fields: Map<Field<*>, Int>) {
    private val data = arrayOfNulls<Any?>(fields.size)

    operator fun <T> get(field: Field<T>): T {
        val index = fields[field] ?: error("'$field' not in result set")
        val value = data[index]
        if (value == null && field is Column<*> && !field.nullable) error("'$field' is null in result set")
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    companion object {
        fun readFrom(rs: ResultSet, fields: List<Field<*>>): ResultRow {
            val indices = fields.mapIndexed { index, col -> col to index }.toMap()

            return ResultRow(indices).apply {
                indices.forEach { (field, index) ->
                    val value = field.get(rs, index + 1)
                    data[index] = value
                }
            }
        }
    }
}


fun ResultSet.read(fields: List<Field<*>>): List<ResultRow> {
    val results = mutableListOf<ResultRow>()
    while (next()) {
        results += ResultRow.readFrom(this, fields)
    }
    return results.toList()
}

