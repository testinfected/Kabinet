package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import kotlin.test.Test

class QueryAliasTest {

    @Test
    fun `builds aliased sub query expression`() {
        val alias = Select.from(Products).alias("things")

        assertThat(alias.toSql(), equalTo(
            """(SELECT products.id, products.number, products.name, products.description FROM products) AS things"""))
    }

    @Test
    fun `tells columns and fields apart`() {
        val alias = Select.from(Products.slice(Products + intLiteral("count(*)"))).alias("things")

        assertThat("all fields", alias.fields, hasSize(equalTo(Products.columns.size + 1)))
        assertThat("columns only", alias.columns, hasSize(equalTo(Products.columns.size)))
    }

    @Test
    fun `aliases original table columns`() {
        val alias = Products.selectAll().alias("things")

        assertThat("fields", alias.fields, equalTo(Products.alias("things").columns))
    }

    @Test
    fun `leaves non column fields unchanged`() {
        val alias = Select.from(Products.slice(intLiteral("count(*)"))).alias("things")

        assertThat(alias.fields, equalTo(listOf(intLiteral("count(*)"))))
    }

    @Test
    fun `retrieves aliased column from original column`() {
        val alias = Select.from(Products.slice(Products.name)).alias("things")

        assertThat("columns", listOf(alias[Products.name]), equalTo(alias.columns))
        assertThat("sql", alias[Products.name].toSql(), equalTo("things.name"))
    }

    @Test
    fun `retrieves aliased fields from original column`() {
        val breed = Products.name.alias("breed")
        val alias = Select.from(Products.slice(breed)).alias("dogs")

        assertThat("columns", listOf(alias[breed]), equalTo(alias.fields))
        assertThat("sql", alias[breed].toSql(), equalTo("dogs.breed"))
    }
}
