package com.vtence.kabinet

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
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
    val transaction = JdbcTransactor(connection)

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
    fun `using string columns`() {
        val persisted = roundTrip(husky)
        assertThat("name", persisted.name, equalTo("Siberian Husky"))
    }

    @Test
    fun `using decimal columns`() {
        val persisted = roundTrip(Item(productId = persist(husky), number = "99999999", price = BigDecimal("649.99")))
        assertThat("price", persisted.price, equalTo(BigDecimal("649.99")))
    }

    @Test
    fun `using null decimal columns`() {
        val persisted = roundTrip(Item(productId = persist(husky), number = "99999999"))
        assertThat("price", persisted.price, absent())
    }

    private fun persist(product: Product): Int {
        return transaction {
            Products.insert(product.record).execute(recorder) get Products.id
        }
    }

    private fun persist(item: Item): Int {
        return transaction {
            Items.insert(item.record).execute(recorder) get Items.id
        }
    }

    private fun roundTrip(product: Product): Product {
        val id = persist(product)
        return checkNotNull(Select.from(Products).where("id = ?", id).firstOrNull(recorder) { product })
    }

    private fun roundTrip(item: Item): Item {
        val id = persist(item)
        return checkNotNull(Select.from(Items).where("id = ?", id).firstOrNull(recorder) { item })
    }
}