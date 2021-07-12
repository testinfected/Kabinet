package com.vtence.kabinet

import java.sql.JDBCType
import java.sql.PreparedStatement


fun PreparedStatement.setParameters(parameters: List<*>, offset: Int = 0) {
    for (i in parameters.indices) {
        this[i + offset + 1] = parameters[i]
    }
}


operator fun PreparedStatement.set(index: Int, value: Any?) {
    val sqlType = parameterMetaData.getParameterType(index)
    setObject(index, value, sqlType)
}


@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
fun JDBCType.toSql(value: Any?): String {
    if (value == null) return "NULL"

    return when(this) {
        JDBCType.BIT -> TODO()
        JDBCType.TINYINT, JDBCType.SMALLINT, JDBCType.INTEGER -> IntColumnType.toNonNullSql(value)
        JDBCType.BIGINT -> TODO()
        JDBCType.FLOAT -> TODO()
        JDBCType.REAL -> TODO()
        JDBCType.DOUBLE -> TODO()
        JDBCType.NUMERIC -> TODO()
        JDBCType.DECIMAL -> TODO()
        JDBCType.CHAR -> TODO()
        JDBCType.VARCHAR, JDBCType.LONGVARCHAR -> StringColumnType.toNonNullSql(value)
        JDBCType.DATE -> TODO()
        JDBCType.TIME -> TODO()
        JDBCType.TIMESTAMP -> TODO()
        JDBCType.BINARY -> TODO()
        JDBCType.VARBINARY -> TODO()
        JDBCType.LONGVARBINARY -> TODO()
        JDBCType.NULL -> "NULL"
        JDBCType.OTHER -> TODO()
        JDBCType.JAVA_OBJECT -> TODO()
        JDBCType.DISTINCT -> TODO()
        JDBCType.STRUCT -> TODO()
        JDBCType.ARRAY -> TODO()
        JDBCType.BLOB -> TODO()
        JDBCType.CLOB -> "'$value'"
        JDBCType.REF -> TODO()
        JDBCType.DATALINK -> TODO()
        JDBCType.BOOLEAN -> TODO()
        JDBCType.ROWID -> TODO()
        JDBCType.NCHAR -> TODO()
        JDBCType.NVARCHAR -> TODO()
        JDBCType.LONGNVARCHAR -> TODO()
        JDBCType.NCLOB -> TODO()
        JDBCType.SQLXML -> TODO()
        JDBCType.REF_CURSOR -> TODO()
        JDBCType.TIME_WITH_TIMEZONE -> TODO()
        JDBCType.TIMESTAMP_WITH_TIMEZONE -> TODO()
    }
}
