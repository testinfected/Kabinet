package com.vtence.kabinet

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

val product = Products
val alternate = Products.alias("alternate")
val item = Items
val orders = Orders

class ResultRowTest {

    val row = ResultRow(
        mapOf(
            product.id to 0,
            product.name to 1,
            alternate[product.number] to 2,
            product.description to 3,
            item.id to 4,
            item.number to 5,
            orders.id to 6,
            orders.number to 7,
            orders.placedAt to 8
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
    fun `tells if value is non null`() {
        row[product.id] = 1
        assertThat("existing value", row.contains(product.id), equalTo(true))

        row[product.name] = null
        assertThat("missing non nullable value", row.contains(product.name), equalTo(false))

        row[product.description] = null
        assertThat("missing nullable value", row.contains(product.description), equalTo(false))
    }

    @Test
    fun `tells if any value from field set is non null`() {
        row[orders.id] = null
        row[orders.number] = null
        row[orders.placedAt] = null

        assertThat("with only null values", row.contains(orders), equalTo(false))

        row[orders.id] = 1
        assertThat("with non null values", row.contains(orders), equalTo(true))
    }

    @Test
    fun `replaces original table fields with aliased fields, dropping original fields`() {
        row[product.id] = 1
        row[product.name] = "Frenchie"
        row[alternate[product.number]] = "77777777"

        row[item.id] = 42
        row[item.number] = "..."

        val rebase = row.rebase(alternate)

        assertThat("product number", rebase[product.number], equalTo("77777777"))
        assertThat("product id", { rebase[product.id] }, throws<IllegalStateException>())
        assertThat("product name", { rebase[product.id] }, throws<IllegalStateException>())
    }

    @Test
    fun `rebasing preserves other fields and tolerates nulls`() {
        row[product.id] = 42
        row[product.name] = ".."
        row[alternate[product.number]] = "..."

        row[item.id] = 1
        row[item.number] = null

        val rebase = row.rebase(alternate)
        assertThat("item number", rebase[item.id], equalTo(1))
        assertThrows<IllegalStateException> { rebase[item.number] }
    }
}
