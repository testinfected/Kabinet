package com.vtence.kabinet

import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.SQLType


interface ColumnType<T> {
    val sqlType: SQLType

    fun set(statement: PreparedStatement, index: Int, value: Any?) {
        if (value != null)
            statement.setObject(index, value, sqlType.vendorTypeNumber)
        else
            statement.setNull(index, sqlType.vendorTypeNumber)
    }
}


object StringType: ColumnType<String?> {
    override val sqlType: SQLType = JDBCType.VARCHAR
}


object IntType: ColumnType<Int?> {
    override val sqlType: SQLType = JDBCType.INTEGER
}

