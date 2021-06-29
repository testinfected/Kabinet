package com.vtence.kabinet

import Product
import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import com.vtence.kabinet.ProductThat.hasDescription
import com.vtence.kabinet.ProductThat.hasNumber
import com.vtence.kabinet.Products.number
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class UpdateTest {
    val database = Database.inMemory()
    val connection = database.openConnection()
    val transaction = JDBCTransactor(connection)

    @BeforeTest
    fun prepareDatabase() {
        database.migrate()
    }

    @AfterTest
    fun closeConnection() {
        connection.close()
    }

    @Test
    fun `updating all records partially`() {
        persist(Product(number = "11111111", name = "English Bulldog"))
        persist(Product(number = "77777777", name = "French Bulldog"))

        val updated: Int = transaction {
            Products.update {
                it[description] = "A companion for kids"
            }.execute(connection)
        }
        assertThat("update count", updated, equalTo(2))

        val records = selectAllProducts()
        assertThat("updated products", records, allElements(hasDescription("A companion for kids")))
    }

    @Test
    fun `updating a specific existing record`() {
        persist(Product(number = "11111111", name = "English Bulldog"))
        persist(Product(number = "77777777", name = "French Bulldog"))

        transaction {
            val updated: Int = Products.updateWhere("$number = ?", "77777777") {
                it[description] = "A miniature Bulldog"
            }.execute(connection)

            assertThat("update count", updated, equalTo(1))
        }

        val records = selectAllProducts()
        assertThat("updated record", records, anyElement(
            hasDescription("A miniature Bulldog") and hasNumber("77777777")))
        assertThat("original record", records, anyElement(
            hasDescription(absent()) and hasNumber("11111111")))
    }

    private fun persist(product: Product) {
        transaction {
            Products.insert(product.record).execute(connection)
        }
    }

    private fun selectAllProducts(): List<Product> {
        return Products.selectAll().list(connection) {
            Product(
                id = it.getInt("id"),
                number = it.getString("number"),
                name = it.getString("name"),
                description = it.getString("description"),
            )
        }
    }
}
