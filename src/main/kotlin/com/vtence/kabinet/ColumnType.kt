package com.vtence.kabinet

import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLType


interface ColumnType<T> {
    val sqlType: SQLType

    fun nullable(): ColumnType<T?>

    fun set(statement: PreparedStatement, index: Int, value: Any?) {
        if (value != null)
            statement.setObject(index, value, sqlType.vendorTypeNumber)
        else
            statement.setNull(index, sqlType.vendorTypeNumber)
    }

    fun get(rs: ResultSet, index: Int): T?

    fun toSql(value: Any?): String = when(value) {
        null -> "NULL"
        else -> toNonNullSql(value)
    }

    fun toNonNullSql(value: Any) = value.toString()
}


object ObjectColumnType: ColumnType<Any> {
    override val sqlType = JDBCType.JAVA_OBJECT

    @Suppress("UNCHECKED_CAST")
    override fun nullable() = this as ColumnType<Any?>

    override fun get(rs: ResultSet, index: Int): Any? {
        return rs.getObject(index)
    }

    override fun toNonNullSql(value: Any): String = buildString {
        when (value) {
            is String -> StringColumnType.toNonNullSql(value)
            is Boolean -> BooleanColumnType.toNonNullSql(value)
            is Int, Long -> IntColumnType.toNonNullSql(value)
            // TODO
            else -> append(value.toString())
        }
    }
}


object BooleanColumnType: ColumnType<Boolean> {
    override val sqlType = JDBCType.BOOLEAN

    @Suppress("UNCHECKED_CAST")
    override fun nullable() = this as ColumnType<Boolean?>

    override fun get(rs: ResultSet, index: Int): Boolean? {
        val value = rs.getBoolean(index)
        return if (rs.wasNull()) null else value
    }
}


object StringColumnType: ColumnType<String> {
    override val sqlType = JDBCType.VARCHAR

    @Suppress("UNCHECKED_CAST")
    override fun nullable() = this as ColumnType<String?>

    override fun get(rs: ResultSet, index: Int): String? {
        return rs.getString(index)
    }

    override fun toNonNullSql(value: Any): String = buildString {
        append('\'')
        append(value.toString())
        append('\'')
    }
}


object IntColumnType: ColumnType<Int> {
    override val sqlType: SQLType = JDBCType.INTEGER

    @Suppress("UNCHECKED_CAST")
    override fun nullable() = this as ColumnType<Int?>

    override fun get(rs: ResultSet, index: Int): Int? {
        val value = rs.getInt(index)
        return if (rs.wasNull()) null else value
    }
}

