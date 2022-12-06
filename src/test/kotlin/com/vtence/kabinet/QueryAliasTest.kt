package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import kotlin.test.Test

class QueryAliasTest {

    @Test
    fun `builds aliased sub query expression`() {
        val subQuery = QueryAlias(Select.from(Products), "things")

        assertThat(subQuery.toSql(), equalTo(
            """(SELECT products.id, products.number, products.name, products.description FROM products) AS things"""))
    }

    @Test
    fun `tells columns and fields apart`() {
        val subQuery = QueryAlias(Select.from(Products.slice(Products + intLiteral("count(*)"))), "things")

        assertThat("all fields", subQuery.fields, hasSize(equalTo(Products.columns.size + 1)))
        assertThat("columns only", subQuery.columns, hasSize(equalTo(Products.columns.size)))
    }

    @Test
    fun `aliases original table columns`() {
        val subQuery = QueryAlias(Products.selectAll(), "things")

        assertThat("fields", subQuery.fields, equalTo(Products.alias("things").columns))
    }

    @Test
    fun `leaves non column fields unchanged`() {
        val subQuery = QueryAlias(Select.from(Products.slice(intLiteral("count(*)"))), "things")

        assertThat(subQuery.fields, equalTo(listOf(intLiteral("count(*)"))))
    }
}

private fun Expression<*>.toSql() = SqlStatementBuilder(prepared = false).also { it.append(this) }.asSql()