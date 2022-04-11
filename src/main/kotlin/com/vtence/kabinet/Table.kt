package com.vtence.kabinet

import java.math.BigDecimal
import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.TimeZone


interface Field<T> : Expression {
    fun get(rs: ResultSet, index: Int): T?
}


interface FieldSet : Expression {
    val source: ColumnSet

    val fields: List<Field<*>>
}


interface ColumnSet : FieldSet {
    val columns: List<Column<*>>

    override val fields: List<Field<*>> get() = columns
}

fun ColumnSet.autoGeneratedColumns(): List<Column<*>> = columns.filter { it.isAutogenerated }

fun ColumnSet.nonAutoGeneratedColumns(): List<Column<*>> = columns - autoGeneratedColumns()


open class Table(name: String) : ColumnSet {
    open val tableName = name
    private val _columns = mutableListOf<Column<*>>()

    override val source: ColumnSet get() = this
    override val columns: List<Column<*>> get() = _columns

    override fun build(statement: SqlBuilder) = statement {
        +tableName
    }

    fun int(name: String): Column<Int> = addColumn(name, IntColumnType)

    fun string(name: String): Column<String> = addColumn(name, StringColumnType)

    fun boolean(name: String): Column<Boolean> = addColumn(name, BooleanColumnType)

    fun decimal(name: String, precision: Int, scale: Int): Column<BigDecimal> =
        addColumn(name, DecimalColumnType(precision, scale))

    fun timestamp(name: String): Column<Instant> = addColumn(name, InstantColumnType)

    fun date(name: String): Column<LocalDate> = addColumn(name, LocalDateColumnType)

    fun time(name: String): Column<LocalTime> = addColumn(name, LocalTimeColumnType)

    @Suppress("UNCHECKED_CAST")
    open operator fun <T : Any?> get(column: Column<T>): Column<T> =
        columns.find { it == column } as? Column<T> ?: error("Column `$column` not found in table `$tableName`")

    open fun <T> addColumn(name: String, type: ColumnType<T>): Column<T> = add(Column(this, name, type))

    private fun <T> add(column: Column<T>): Column<T> = column.also { _columns += it }

    fun replaceColumn(old: Column<*>, new: Column<*>): Unit = with(_columns) {
        remove(old)
        add(new)
    }

    override fun toString(): String {
        return "table $tableName"
    }
}


class Slice(override val source: ColumnSet, override val fields: List<Field<*>>) : FieldSet {
    override fun build(statement: SqlBuilder) = statement {
        +source
    }
}

fun ColumnSet.slice(field: Field<*>, vararg more: Field<*>): FieldSet = slice(listOf(field) + more)

fun ColumnSet.slice(set: FieldSet): FieldSet = Slice(this, set.fields)

fun ColumnSet.slice(fields: List<Field<*>>): FieldSet = Slice(this, fields)

fun ColumnSet.select(column: Field<*>, vararg more: Field<*>): Select = Select.from(slice(column, *more))


class TableSlice(override val source: Table, columns: List<Column<*>>) : ColumnSet {
    override val columns: List<Column<*>> = columns.map { source[it] }

    override fun build(statement: SqlBuilder) = statement {
        +source
    }
}

fun Table.slice(column: Column<*>, vararg more: Column<*>): ColumnSet = slice(listOf(column) + more)

fun Table.slice(columns: List<Column<*>>): ColumnSet = TableSlice(this, columns)


class Join(
    private val table: ColumnSet,
    private val otherTable: ColumnSet,
    private val type: JoinType = JoinType.INNER,
    private val condition: Expression
) : ColumnSet {

    override val source: ColumnSet = this

    override val columns: List<Column<*>> get() = table.columns + otherTable.columns

    override fun build(statement: SqlBuilder) = statement {
        +table
        append(" $type JOIN ")
        +otherTable
        append(" ON ")
        +condition
    }
}

enum class JoinType {
    INNER,
    LEFT,
}

class JoinPart(
    private val onColumn: Column<*>,
    private val otherColumn: Column<*>,
    private val additionalConstraint: Expression?
) : Expression {
    override fun build(statement: SqlBuilder) = statement {
        +onColumn
        +" = "
        +otherColumn
        additionalConstraint?.let {
            +" AND "
            +additionalConstraint
        }
    }
}


fun ColumnSet.join(otherTable: ColumnSet, condition: Expression): Join {
    return Join(this, otherTable, JoinType.INNER, condition)
}

fun ColumnSet.join(
    otherTable: ColumnSet,
    onColumn: Column<*>,
    otherColumn: Column<*>,
    additionalConstraint: Expression? = null
): Join {
    return join(otherTable, JoinPart(onColumn, otherColumn, additionalConstraint))
}

fun ColumnSet.join(
    otherTable: ColumnSet,
    onColumn: Column<*>,
    otherColumn: Column<*>,
    additionalConstraint: String? = null,
    vararg parameters: Any?
): Join = join(otherTable, onColumn, otherColumn, additionalConstraint?.asExpression(*parameters))


fun ColumnSet.join(otherTable: ColumnSet, condition: String, vararg parameters: Any?): Join =
    join(otherTable, condition.asExpression(*parameters))



fun ColumnSet.leftJoin(otherTable: ColumnSet, condition: Expression): Join {
    return Join(this, otherTable, JoinType.LEFT, condition)
}

fun ColumnSet.leftJoin(
    otherTable: ColumnSet,
    onColumn: Column<*>,
    otherColumn: Column<*>,
    additionalConstraint: Expression? = null
): Join {
    return leftJoin(otherTable, JoinPart(onColumn, otherColumn, additionalConstraint))
}

fun ColumnSet.leftJoin(
    otherTable: ColumnSet,
    onColumn: Column<*>,
    otherColumn: Column<*>,
    additionalConstraint: String? = null,
    vararg parameters: Any?
): Join = leftJoin(otherTable, onColumn, otherColumn, additionalConstraint?.asExpression(*parameters))


fun ColumnSet.leftJoin(otherTable: ColumnSet, condition: String, vararg parameters: Any?): Join =
    leftJoin(otherTable, condition.asExpression(*parameters))
