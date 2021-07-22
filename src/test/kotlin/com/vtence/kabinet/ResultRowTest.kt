package com.vtence.kabinet

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import kotlin.test.Test

val product = Products
val alternate = Products.alias("alternate")
val item = Items

class ResultRowTest {

    val row = ResultRow(
        mapOf(
            product.id to 0,
            product.name to 1,
            alternate[product.number] to 2,
            product.description to 3,
            item.id to 4,
            item.number to 5
        )
    )

    @Test
    fun `retrieves value by field`() {
        row[product.name] = "Frenchie"

        assertThat(row[product.name], equalTo("Frenchie"))
    }

    @Test
    fun `defaults to null`() {
        assertThat(row[product.description], absent())
    }

    @Test
    fun `complains of missing field`() {
        assertThat({ row[product.number] }, throws<IllegalStateException>())
    }

    @Test
    fun `complains of null value for non-nullable column`() {
        assertThat({ row[product.name] }, throws<IllegalStateException>())
    }

    @Test
    fun `replaces original table fields with aliased fields, dropping original fields but preserving others`() {
        row[product.id] = 1
        row[product.name] = "Frenchie"
        row[alternate[product.number]] = "77777777"
        row[item.id] = 1
        row[item.number] = "1000000"

        val rebase = row.rebase(alternate)
        assertThat("product number", rebase[product.number], equalTo("77777777"))
        assertThat("item number", rebase[item.number], equalTo("1000000"))

        assertThat("product id", { rebase[product.id] }, throws<IllegalStateException>())
        assertThat("product name", { rebase[product.id] }, throws<IllegalStateException>())
    }
}
