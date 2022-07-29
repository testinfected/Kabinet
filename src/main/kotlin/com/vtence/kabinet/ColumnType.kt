package com.vtence.kabinet

import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


abstract class ColumnType<T> {
    abstract val sqlType: SQLType

    abstract fun nullable(): ColumnType<T?>

    open fun set(statement: PreparedStatement, index: Int, value: Any?) {
        if (value != null)
            statement.setObject(index, toJdbc(value), sqlType.vendorTypeNumber)
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

    open fun toJdbc(value: Any) = value
}


object AutoDetectColumnType : ColumnType<Any>() {
    override val sqlType = JDBCType.OTHER

    @Suppress("UNCHECKED_CAST")
    override fun nullable() = this as ColumnType<Any?>

    override fun get(rs: ResultSet, index: Int): Any? {
        return rs.getObject(index)
    }

    override fun set(statement: PreparedStatement, index: Int, value: Any?) {
        if (value != null)
            statement.setObject(index, value)
        else
            statement.setObject(index, null)
    }

    override fun toNonNullSql(value: Any): String = when (value) {
        is String -> StringColumnType.toNonNullSql(value)
        is Boolean -> BooleanColumnType.toNonNullSql(value)
        is Int, Long -> IntColumnType.toNonNullSql(value)
        is BigDecimal -> DecimalColumnType(value.precision(), value.scale()).toNonNullSql(value)
        is LocalDate -> LocalDateColumnType.toNonNullSql(value)
        is Instant -> InstantColumnType.toNonNullSql(value)
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

    override fun toNonNullSql(value: Any): String {
        return value.toString().uppercase()
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
        return rs.getBigDecimal(index)?.setScale(scale, RoundingMode.HALF_EVEN)
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


/**
 * Store `Instant` as a timestamp within UTC timezone, since it represents a point on the timeline.
 * `LocalDate` uses a timestamp (without timezone) instead.
 */
object InstantColumnType : ColumnType<Instant>() {
    override val sqlType = JDBCType.TIMESTAMP_WITH_TIMEZONE

    // Since instant is a point on the timeline, tell the JDBC driver
    // to use the UTC timezone
    private val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

    @Suppress("UNCHECKED_CAST")
    override fun nullable() = this as ColumnType<Instant?>

    override fun toJdbc(value: Any): Any {
        return when(value) {
            is Instant -> Timestamp.from(value)
            else -> value
        }
    }

    override fun set(statement: PreparedStatement, index: Int, value: Any?) {
        when(value) {
            is Instant -> statement.setTimestamp(index, Timestamp.from(value), utc)
            null -> statement.setNull(index, sqlType.vendorTypeNumber)
            else -> statement.setObject(index, value, sqlType.vendorTypeNumber)
        }
    }


    override fun get(rs: ResultSet, index: Int): Instant? {
        return rs.getTimestamp(index, utc)?.toInstant()
    }

    override fun toNonNullSql(value: Any): String {
        val instant = when(value) {
            is Instant -> value
            else -> error("Unexpected value: $value of ${value::class.qualifiedName}")
        }
        return "'${Timestamp.from(instant)}'"
    }
}


object LocalDateColumnType : ColumnType<LocalDate>() {
    override val sqlType = JDBCType.DATE

    @Suppress("UNCHECKED_CAST")
    override fun nullable() = this as ColumnType<LocalDate?>

    override fun get(rs: ResultSet, index: Int): LocalDate? {
        return rs.getObject(index, LocalDate::class.java)
    }

    override fun toNonNullSql(value: Any): String {
        val date = when (value) {
            is LocalDate -> value
            else -> error("Unexpected value: `$value` has type ${value::class.qualifiedName}")
        }

        return "'${ISO_DATE_FORMATTER.format(date)}'"
    }

    private val ISO_DATE_FORMATTER by lazy {
        DateTimeFormatter.ISO_LOCAL_DATE.withLocale(Locale.ROOT).withZone(ZoneId.systemDefault())
    }
}


object LocalTimeColumnType : ColumnType<LocalTime>() {
    override val sqlType = JDBCType.TIME

    @Suppress("UNCHECKED_CAST")
    override fun nullable() = this as ColumnType<LocalTime?>

    override fun get(rs: ResultSet, index: Int): LocalTime? {
        return rs.getTime(index)?.toLocalTime()
    }

    override fun toNonNullSql(value: Any): String {
        val instant = when (value) {
            is LocalTime -> value
            else -> error("Unexpected value `$value` has type ${value::class.qualifiedName}")
        }
        return "'${ISO_TIME_FORMATTER.format(instant)}'"
    }

    private val ISO_TIME_FORMATTER by lazy {
        DateTimeFormatter.ISO_LOCAL_TIME.withLocale(Locale.ROOT).withZone(ZoneId.systemDefault())
    }
}
