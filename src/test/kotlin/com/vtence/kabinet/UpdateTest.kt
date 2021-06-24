package com.vtence.kabinet

import Product
import com.natpryce.hamkrest.allElements
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
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
    fun `updating a record partially`() {
        persist(Product(number = "11111111", name = "English Bulldog", description = "A cute, small playful dog"))

        val updated: Int = transaction {
            Products.update {
                it[name] = "French Bulldog"
            }.execute(connection)
        }
        assertThat("update count", updated, equalTo(1))

        val records = selectAllProducts()
        assertThat("updated product", records, allElements(ProductThat.hasName("French Bulldog")))
    }

    private fun persist(product: Product) {
        transaction {
            Products.insert {
                it[number] = product.number
                it[description] = product.description
                it[name] = product.name
            }.execute(connection)
        }
    }

    private fun selectAllProducts(): List<Product> {
        return sql("SELECT * FROM products").list(connection) {
            Product(
                id = it.getInt("id"),
                number = it.getString("number"),
                name = it.getString("name"),
                description = it.getString("description"),
            )
        }
    }
}
