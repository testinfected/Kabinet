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
        val value = getOrNull(field)
        if (value == null && field is Column<*> && !field.isNullable) error("'$field' is null in result set")
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    private fun <T> getOrNull(field: Field<T>): Any? {
        val index = fields[field] ?: error("'$field' not in result set")
        return data[index]
    }

    operator fun <T> set(field: Field<out T>, value: T) {
        val index = fields[field] ?: error("$field is not in record set")
        data[index] = value
    }

    fun rebase(alias: TableAlias<*>): ResultRow {
        val mapping = fields.mapNotNull { (field, _) ->
            val column = field as? Column<*>
            val original = column?.let { alias.originalColumn(it) }
            val value = getOrNull(field)
            when {
                original != null -> original to value
                column?.table == alias.delegate -> null
                else -> field to value
            }
        }.toMap()

        return createFrom(mapping)
    }

    companion object {
        fun readFrom(rs: ResultSet, fields: List<Field<*>>): ResultRow {
            val indices = fields.mapIndexed { index, col -> col to index }.toMap()

            return ResultRow(indices).also { row ->
                indices.forEach { (field, index) ->
                    row[field] = field.get(rs, index + 1)
                }
            }
        }

        fun createFrom(data: Map<Field<*>, Any?>): ResultRow =
            ResultRow(data.keys.mapIndexed { index, field -> field to index }.toMap()).also { row ->
                data.forEach { (field, value) -> row[field] = value }
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


operator fun <T> ResultRow.get(hydrator: Hydrator<T>): T = hydrator(this)
