package com.vtence.kabinet

import java.sql.ResultSet


class Column<T>(
    val table: Table,
    val name: String,
    val type: ColumnType<T>,
    autoGenerated: Boolean = false,
    nullable: Boolean = false
) : Field<T> {

    val qualifiedName: String = "${table.tableName}.$name"
    val isAutogenerated = autoGenerated
    val isNullable = nullable

    fun autoGenerated(): Column<T> =
        Column(table, name, type, true, isNullable).also { replaceBy(it) }

    override fun get(rs: ResultSet, index: Int): T? {
        return type.get(rs, index)
    }

    fun nullable(): Column<T?> =
        Column(table, name, type.nullable(), isAutogenerated, true).also { replaceBy(it) }

    private fun replaceBy(column: Column<*>) = table.replaceColumn(this, column)

    override fun build(statement: SqlStatement) = statement {
        +qualifiedName
    }

    fun unqualified(): Expression = Expression { it.append(name) }

    override fun toString(): String = qualifiedName

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Column<*>) return false

        return table == other.table && name == other.name
    }

    override fun hashCode(): Int = 31 * table.hashCode() + name.hashCode()
}
