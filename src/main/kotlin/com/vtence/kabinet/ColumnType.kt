package com.vtence.kabinet

import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLType


interface ColumnType<T> {
    val sqlType: SQLType

    fun set(statement: PreparedStatement, index: Int, value: Any?) {
        if (value != null)
            statement.setObject(index, value, sqlType.vendorTypeNumber)
        else
            statement.setNull(index, sqlType.vendorTypeNumber)
    }

    fun get(rs: ResultSet, index: Int): T?
}


object StringType: ColumnType<String> {
    override val sqlType: SQLType = JDBCType.VARCHAR

    override fun get(rs: ResultSet, index: Int): String? {
        return rs.getString(index)
    }
}


object IntType: ColumnType<Int> {
    override val sqlType: SQLType = JDBCType.INTEGER

    override fun get(rs: ResultSet, index: Int): Int? {
        val value = rs.getInt(index)
        return if (rs.wasNull()) null else value
    }
}

