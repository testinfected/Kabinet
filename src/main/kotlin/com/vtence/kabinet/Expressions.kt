package com.vtence.kabinet

import java.sql.ResultSet


class Literal<T>(private val expression: String, private val type: ColumnType<T>) : Field<T> {
    override fun get(rs: ResultSet, index: Int): T? {
        return type.get(rs, index)
    }

    override fun build(statement: SqlStatement) = statement {
        +expression
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


fun intLiteral(expression: String) = Literal(expression, IntColumnType)


class PreparedExpression(private val sql: String, private val parameters: List<Any?>) : Expression {
    override fun build(statement: SqlStatement) = statement {
        questionMarksOutsideQuotes.split(sql).forEachIndexed { index, fragment ->
            +fragment
            parameters.getOrNull(index)?.let { +it.asArgument() }
        }
    }

    companion object {
        /**
         * We're looking for question marks with a little bit of regex magic.
         *
         * Using negative look behind and look ahead we verify question mark is not doubled
         * (i.e. not preceded or followed by another question mark):
         *
         *     (?<!\?)\?(?!\?)
         *
         * Using positive lookahead we make sure it's followed until the end of the string
         * by an even number of single or double quotes (which in turn are not themselves quoted),
         * assuming quotes are properly balanced:
         *
         *     (?=(?:[^"']*(["'])(?:(?!\1).)*\1)*[^"']*$)
         *
         * The magic lies in the repeated negative lookahead on the matching opening quote to
         * find the corresponding closing quote:
         *
         *     (?:(?!\1).)*
         */
        val questionMarksOutsideQuotes =
            Regex("""(?<!\?)\?(?!\?)(?=(?:[^"']*(["'])(?:(?!\1).)*\1)*[^"']*$)""")
    }
}


fun String.asExpression(vararg parameters: Any?) = asExpression(parameters.toList())

fun String.asExpression(parameters: List<Any?>) = PreparedExpression(this, parameters)


class QueryParameter<T : Any>(
    private val value: T?,
    private val type: ColumnType<T>
) : Expression {

    override fun build(statement: SqlStatement) = statement {
        appendArgument(type, value)
    }
}

