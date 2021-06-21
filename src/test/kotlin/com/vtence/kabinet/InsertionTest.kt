package com.vtence.kabinet

import Product
import com.natpryce.hamkrest.allOf
import com.natpryce.hamkrest.assertion.assertThat
import kotlin.test.*

class InsertionTest {
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
    fun `inserting a new record`() {
        val id = transaction {
            Products.insert {
                it.setString(1, "12345678")
                it.setString(2, "English Bulldog")
                it.setString(3, "A muscular, heavy dog")
            }.execute(connection) {
                it.getInt(1)
            }
        }

        val allProducts = sql(
            """
                SELECT * FROM products
            """.trimIndent()
        )
        val found = allProducts.list(connection) {
            Product(
                id = it.getInt("id"),
                number = it.getString("number"),
                name = it.getString("name"),
                description = it.getString("description"),
            )
        }.single()

        assertThat(
            "inserted product", found, allOf(
                ProductThat.hasId(id),
                ProductThat.hasNumber("12345678"),
                ProductThat.hasName("English Bulldog"),
                ProductThat.hasDescription("A muscular, heavy dog")
            )
        )
    }
}
