package com.vtence.kabinet

class QueryAlias(private val query: Query<*>, private val alias: String) : ColumnSet {

    override val fields: List<Field<*>>
        get() = query.set.fields.map { (it as? Column<*>)?.aliased() ?: it }

    override val columns: List<Column<*>>
        get() = fields.filterIsInstance<Column<*>>()

    override val source: ColumnSet
        get() = this

    override fun build(statement: SqlBuilder) = statement {
        append("(", query, ")", " AS $alias")
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any?> get(original: Column<T>): Column<T> {
        return query.set.source.columns.find { it == original }?.aliased() as? Column<T> ?: error("Column $original not found")
    }

    private fun <T : Any?> Column<T>.aliased() = Column(table.alias(alias), name, type)
}


fun Query<*>.alias(alias: String) = QueryAlias(this, alias)