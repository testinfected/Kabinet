package com.vtence.kabinet

import Product
import com.natpryce.hamkrest.and
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
    fun `inserting a new record by building the dataset`() {
        val id = transaction {
            Products.insert {
                it[number] = "12345678"
                it[description] = "A muscular, heavy dog"
                it[name] = "English Bulldog"
            }.execute(connection)
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
            "inserted product", found,
            ProductThat.hasId(id) and
                    ProductThat.hasNumber("12345678") and
                    ProductThat.hasName("English Bulldog") and
                    ProductThat.hasDescription("A muscular, heavy dog")
        )
    }

    val Product.dataset get() = dataset(this)

    fun dataset(product: Product): Products.(Dataset) -> Unit = {
        it[number] = product.number
        it[description] = product.description
        it[name] = product.name
    }

    val bulldog = Product(number = "12345678", name = "English Bulldog", description = "A muscular, heavy dog")

    @Test
    fun `inserting again, this time using a dataset definition`() {
        val id = transaction {
            Products.insert(bulldog.dataset).execute(connection)
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
            "inserted product", found,
            ProductThat.hasId(id) and
                    ProductThat.hasNumber("12345678") and
                    ProductThat.hasName("English Bulldog") and
                    ProductThat.hasDescription("A muscular, heavy dog")
        )
    }
}
