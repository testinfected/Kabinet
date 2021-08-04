package com.vtence.kabinet

import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLType


abstract class ColumnType<T> {
    abstract val sqlType: SQLType

    abstract fun nullable(): ColumnType<T?>

    open fun set(statement: PreparedStatement, index: Int, value: Any?) {
        if (value != null)
            statement.setObject(index, value, sqlType.vendorTypeNumber)
        else
            statement.setNull(index, sqlType.vendorTypeNumber)
    }

    abstract fun get(rs: ResultSet, index: Int): T?

    fun toSql(value: Any?): String = when (value) {
        null -> "NULL"
        else -> toNonNullSql(value)
    }

    open fun toNonNullSql(value: Any) = value.toString()

    override fun toString(): String {
        return sqlType.name
    }
}


object ObjectColumnType : ColumnType<Any>() {
    override val sqlType = JDBCType.JAVA_OBJECT

    @Suppress("UNCHECKED_CAST")
    override fun nullable() = this as ColumnType<Any?>

    override fun get(rs: ResultSet, index: Int): Any? {
        return rs.getObject(index)
    }

    override fun toNonNullSql(value: Any): String = when (value) {
        is String -> StringColumnType.toNonNullSql(value)
        is Boolean -> BooleanColumnType.toNonNullSql(value)
        is Int, Long -> IntColumnType.toNonNullSql(value)
        // TODO
        else -> value.toString()
    }
}


object BooleanColumnType : ColumnType<Boolean>() {
    override val sqlType = JDBCType.BOOLEAN

    @Suppress("UNCHECKED_CAST")
    override fun nullable() = this as ColumnType<Boolean?>

    override fun get(rs: ResultSet, index: Int): Boolean? {
        val value = rs.getBoolean(index)
        return if (rs.wasNull()) null else value
    }
}


object StringColumnType : ColumnType<String>() {
    override val sqlType = JDBCType.VARCHAR

    @Suppress("UNCHECKED_CAST")
    override fun nullable() = this as ColumnType<String?>

    override fun get(rs: ResultSet, index: Int): String? {
        return rs.getString(index)
    }

    override fun toNonNullSql(value: Any): String = buildString {
        append('\'')
        append((value as String).replace("'", "''"))
        append('\'')
    }
}


object IntColumnType : ColumnType<Int>() {
    override val sqlType: SQLType = JDBCType.INTEGER

    @Suppress("UNCHECKED_CAST")
    override fun nullable() = this as ColumnType<Int?>

    override fun get(rs: ResultSet, index: Int): Int? {
        val value = rs.getInt(index)
        return if (rs.wasNull()) null else value
    }
}

class DecimalColumnType(
    /** Significant digits */
    val precision: Int,
    /** Decimal digits in the fractional part */
    val scale: Int
) : ColumnType<BigDecimal>() {
    override val sqlType: SQLType = JDBCType.NUMERIC

    @Suppress("UNCHECKED_CAST")
    override fun nullable() = this as ColumnType<BigDecimal?>

    override fun get(rs: ResultSet, index: Int): BigDecimal? {
        return rs.getBigDecimal(index).setScale(scale, RoundingMode.HALF_EVEN)
    }

    override fun toString(): String = "DECIMAL($precision, $scale)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DecimalColumnType

        if (precision != other.precision) return false
        if (scale != other.scale) return false

        return true
    }

    override fun hashCode(): Int {
        var result = precision
        result = 31 * result + scale
        return result
    }
}
