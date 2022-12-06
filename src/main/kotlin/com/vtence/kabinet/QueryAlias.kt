package com.vtence.kabinet

class QueryAlias(private val query: Query<*>, private val alias: String) : ColumnSet {

    override val fields: List<Field<*>>
        get() = query.set.fields.map { (it as? Column<*>)?.alias() ?: it }

    override val columns: List<Column<*>>
        get() = fields.filterIsInstance<Column<*>>()

    override val source: ColumnSet
        get() = this

    override fun build(statement: SqlBuilder) = statement {
        append("(", query, ")", " AS $alias")
    }

    private fun <T : Any?> Column<T>.alias() = Column(table.alias(alias), name, type)
}


fun Query<*>.alias(alias: String) = QueryAlias(this, alias)