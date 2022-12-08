package com.vtence.kabinet

class QueryAlias(private val query: Query<*>, private val alias: String) : ColumnSet {

    override val fields: List<Field<*>> = query.set.fields.map { field ->
        (field as? Column<*>)?.aliased()
            ?: (field as? FieldAlias<*>)?.fullyQualified()
            ?: field
    }

    override val columns: List<Column<*>> = fields.filterIsInstance<Column<*>>()

    override val source: ColumnSet = this

    override fun build(statement: SqlBuilder) = statement {
        append("(", query, ")", " AS $alias")
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(original: Column<T>): Column<T> {
        return query.set.source.columns.find { it == original }?.aliased() as? Column<T>
            ?: error("Column `$original` not found in original table fields")
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(original: Field<T>): Field<T> {
        val aliases = query.set.fields.filterIsInstance<FieldAlias<T>>()
        return aliases.find { it == original || it.delegate == original }?.fullyQualified()
            ?: error("Field `$original` not found in original table fields")
    }

    private fun <T : Any?> Column<T>.aliased() = Column(table.alias(alias), name, type)

    private fun <T> FieldAlias<T>.fullyQualified() = delegate.alias("${this@QueryAlias.alias}.$alias").aliasOnly()
}


fun Query<*>.alias(alias: String) = QueryAlias(this, alias)