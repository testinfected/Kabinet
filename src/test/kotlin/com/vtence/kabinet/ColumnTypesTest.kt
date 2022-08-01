package com.vtence.kabinet

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import java.math.BigDecimal
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test


/**
 * Tests to demonstrate usages of the various SQL data types.
 */
class DataTypesTest {

    val database = Database.inMemory()
    val connection = database.openConnection()
    val persisted = Persister(connection)

    val recorder = StatementRecorder(connection)

    @BeforeTest
    fun prepareDatabase() {
        database.migrate()
    }

    @AfterTest
    fun closeConnection() {
        connection.close()
    }

    val husky = Product(number = 100345, name = "Siberian Husky")

    @Test
    fun `using int columns`() {
        val persisted = roundTrip(husky)
        assertThat("name", persisted.number, equalTo(100345))
    }

    @Test
    fun `using string columns`() {
        val persisted = roundTrip(husky)
        assertThat("name", persisted.name, equalTo("Siberian Husky"))
    }

    @Test
    fun `using decimal columns`() {
        val persisted = roundTrip(Item(productId = persisted(husky), number = "99999999", price = BigDecimal("649.99")))
        assertThat("price", persisted.price, equalTo(BigDecimal("649.99")))
    }

    @Test
    fun `using boolean columns`() {
        val persisted = roundTrip(Item(productId = persisted(husky), number = "99999999", onSale = true))
        assertThat("sale", persisted.onSale, equalTo(true))
    }

    @Test
    fun `using null decimal columns`() {
        val persisted = roundTrip(Item(productId = persisted(husky), number = "99999999"))
        assertThat("price", persisted.price, absent())
    }

    @Test
    fun `using long columns`() {
        val persisted = roundTrip(Order(number = 1234567890123456L))
        assertThat("name", persisted.number, equalTo(1234567890123456L))
    }

    private fun roundTrip(product: Product): Product {
        val id = persisted(product)
        return checkNotNull(Select.from(Products).where("id = ?", id).firstOrNull(recorder) { it.product })
    }

    private fun roundTrip(item: Item): Item {
        val id = persisted(item)
        return checkNotNull(Select.from(Items).where("id = ?", id).firstOrNull(recorder) { it.item })
    }

    private fun roundTrip(order: Order): Order {
        val id = persisted(order)
        return checkNotNull(Select.from(Orders).where("id = ?", id).firstOrNull(recorder) { it.order })
    }
}