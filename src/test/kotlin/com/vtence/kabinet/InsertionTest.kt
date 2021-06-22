package com.vtence.kabinet

import Product
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.vtence.kabinet.ProductThat.hasDescription
import com.vtence.kabinet.ProductThat.hasId
import com.vtence.kabinet.ProductThat.hasName
import com.vtence.kabinet.ProductThat.hasNumber
import com.vtence.kabinet.ProductThat.hasSameStateAs
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

        val found = selectAllProducts().single()

        assertThat(
            "inserted product", found,
            hasId(id) and
                    hasNumber("12345678") and
                    hasName("English Bulldog") and
                    hasDescription("A muscular, heavy dog")
        )
    }

    val Product.record get() = dataset(this)

    fun dataset(product: Product): Products.(Dataset) -> Unit = {
        it[number] = product.number
        it[description] = product.description
        it[name] = product.name
    }

    val bulldog = Product(number = "12345678", name = "English Bulldog", description = "A muscular, heavy dog")

    @Test
    fun `inserting again, this time using a dataset definition`() {
        val id = transaction {
            Products.insert(bulldog.record).execute(connection)
        }

        val found = selectAllProducts().single()

        assertThat("inserted product", found, hasSameStateAs(bulldog.copy(id = id)))
    }


    @Test
    fun `inserting yet again, this time the old fashion way`() {
        val id = transaction {
            Insert.into(Products) {
                it.setString(1, "12345678")
                it.setString(2, "English Bulldog")
                it.setString(3, "A muscular, heavy dog")
            }.execute(connection)
        }

        val found = selectAllProducts().single()

        assertThat(
            "inserted product", found,
            hasId(id) and
                    hasNumber("12345678") and
                    hasName("English Bulldog") and
                    hasDescription("A muscular, heavy dog")
        )
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
