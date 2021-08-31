package com.vtence.kabinet

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import com.vtence.kabinet.ProductThat.hasDescription
import com.vtence.kabinet.ProductThat.hasNumber
import com.vtence.kabinet.Products.number
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class DeletionTest {
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

    @Test
    fun `deleting all records`() {
        persist(Product(number = 11111111, name = "English Bulldog"))
        persist(Product(number = 77777777, name = "French Bulldog"))
        persist(Product(number = 88888888, name = "Labrador Retriever"))

        val deleted = transaction {
            Products.deleteAll(recorder)
        }

        assertThat("records deleted", deleted, equalTo(3))

        val records = Products.selectAll().list(recorder) { product }
        assertThat("records left", records, isEmpty)
    }

    private fun persist(product: Product) {
        transaction {
            Products.insert(product.record).execute(recorder)
        }
    }
}
