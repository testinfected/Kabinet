package com.vtence.kabinet

import java.sql.ResultSet


interface Field<T>: Expression {
    fun get(rs: ResultSet, index: Int): T?
}


class Literal<T>(private val expression: String, private val type: ColumnType<T>): Expression, Field<T> {
    override fun get(rs: ResultSet, index: Int): T? {
        return type.get(rs, index)
    }

    override fun appendTo(sql: SqlBuilder) {
        sql.append(expression)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Literal<*>) return false

        if (expression != other.expression) return false

        return true
    }

    override fun hashCode(): Int {
        return expression.hashCode()
    }

    override fun toString(): String = expression
}


fun predicate(expression: String) = Literal(expression, BooleanColumnType)
