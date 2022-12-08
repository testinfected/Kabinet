package com.vtence.kabinet

class FieldAlias<T>(val delegate: Field<T>, val alias: String) : Field<T> {
    override val type: ColumnType<T>
        get() = delegate.type

    override fun build(statement: SqlBuilder) = statement {
        append(delegate, " AS $alias")
    }

    fun aliasOnly(): Field<T> = FieldAliasOnly(delegate, alias)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FieldAlias<*>) return false

        if (delegate != other.delegate) return false
        if (alias != other.alias) return false

        return true
    }

    override fun hashCode(): Int = 31 * delegate.hashCode() + alias.hashCode()
}

fun <T> Field<T>.alias(alias: String) = FieldAlias(this, alias)


private class FieldAliasOnly<T>(
    private val delegate: Field<T>,
    private val alias: String
) : Field<T> {
    override val type: ColumnType<T>
        get() = delegate.type

    override fun build(statement: SqlBuilder) = statement {
        append(alias)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FieldAliasOnly<*>) return false

        if (delegate != other.delegate) return false
        if (alias != other.alias) return false

        return true
    }

    override fun hashCode(): Int = 31 * delegate.hashCode() + alias.hashCode()
}

