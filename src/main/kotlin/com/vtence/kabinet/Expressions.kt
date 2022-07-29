package com.vtence.kabinet


class Literal<T>(private val expression: String, override val type: ColumnType<T>) : Field<T> {
    override fun build(statement: SqlBuilder) = statement {
        +expression
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Literal<*>) return false

        if (expression != other.expression) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        return expression.hashCode()
    }

    override fun toString(): String = expression
}


fun intLiteral(expression: String) = Literal(expression, IntColumnType)


class PreparedExpression<T>(private val sql: String, private val params: List<Any?>) : Expression<T> {
    override fun build(statement: SqlBuilder) = statement {
        questionMarksOutsideQuotes.split(sql).forEachIndexed { index, fragment ->
            if (fragment.isNotBlank()) {
                +fragment
                if (index < params.size) +params[index].toArgument(AutoDetectColumnType.nullable())
            }
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
        private val questionMarksOutsideQuotes =
            Regex("""(?<!\?)\?(?!\?)(?=(?:[^"']*(["'])(?:(?!\1).)*\1)*[^"']*$)""")
    }
}


fun <T> String.asExpression(vararg arguments: Any?) = asExpression<T>(arguments.toList())

fun <T> String.asExpression(arguments: Iterable<Any?>) = PreparedExpression<T>(this, arguments.toList())


class QueryParameter<T>(
    private val value: T,
    private val type: ColumnType<T>
) : Expression<T> {

    override fun build(statement: SqlBuilder) = statement {
        appendArgument(type, value)
    }
}

