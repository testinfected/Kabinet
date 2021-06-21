package com.vtence.kabinet

import Product
import com.natpryce.hamkrest.allOf
import com.natpryce.hamkrest.anyElement
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.present
import com.vtence.kabinet.ProductThat.hasDescription
import com.vtence.kabinet.ProductThat.hasId
import com.vtence.kabinet.ProductThat.hasName
import com.vtence.kabinet.ProductThat.hasNumber
import org.junit.jupiter.api.Test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class PlainSqlTest {

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
    fun `inserting and retrieving a single database row with all columns`() {
        val id = transaction {
            val bulldog = sql(
                """
                INSERT INTO products(number, name, description) 
                VALUES('12345678', 'English Bulldog', 'A muscular, heavy dog')
            """.trimIndent()
            )
            bulldog.insert(connection)
        }
        assertThat("id", id, present())

        val products = sql(
            """
                SELECT * FROM products
                WHERE number = :number
            """.trimIndent()
        )
        val found = products.set("number", "12345678").list(connection) {
            Product(
                id = it.getInt("id"),
                number = it.getString("number"),
                name = it.getString("name"),
                description = it.getString("description"),
            )
        }

        assertThat("products found", found, anyElement(allOf(
            hasId(id),
            hasNumber("12345678"),
            hasName("English Bulldog"),
            hasDescription("A muscular, heavy dog")
        )))
    }
}
