package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlin.test.Test

class SelectStatementTest {
    @Test
    fun `selects columns in set`() {
        val select = SelectStatement(Products)

        assertThat(select.toSql(), equalTo(
            "SELECT products.id, products.number, products.name, products.description FROM products"
        ))
    }

    @Test
    fun `supports limits conditions`() {
        val select = SelectStatement(Products).limitTo(1, start = 0)

        assertThat(select.toSql(), equalTo(
            "SELECT products.id, products.number, products.name, products.description FROM products LIMIT 1"
        ))
    }

    @Test
    fun `supports offsets in limits`() {
        val select = SelectStatement(Products)
            .limitTo(10, start = 100)

        assertThat(select.toSql(), equalTo(
            "SELECT products.id, products.number, products.name, products.description FROM products LIMIT 10 OFFSET 100"
        ))
    }

    @Test
    fun `supports where conditions`() {
        val select = SelectStatement(Products).where(lit("a = ? AND b = ?"))

        assertThat(select.toSql(), equalTo(
            "SELECT products.id, products.number, products.name, products.description FROM products WHERE a = ? AND b = ?"
        ))
    }
}
