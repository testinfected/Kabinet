package com.vtence.kabinet

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import com.vtence.kabinet.ProductThat.hasName
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

    val bulldog = Product(number = 11111111, name = "English Bulldog")
    val frenchie = Product(number = 77777777, name = "French Bulldog")
    val lab = Product(number = 88888888, name = "Labrador Retriever")

    @Test
    fun `deleting all records`() {
        persist(bulldog)
        persist(frenchie)
        persist(lab)

        val deleted = transaction {
            Products.deleteAll(recorder)
        }

        assertThat("records deleted", deleted, equalTo(3))
        assertThat("sql", recorder.lastStatement, equalTo("DELETE FROM products"))

        val records = Products.selectAll(recorder) { product }
        assertThat("records left", records, isEmpty)
    }

    @Test
    fun `deleting a specific record`() {
        persist(bulldog)
        persist(frenchie)
        persist(lab)

        val deleted = transaction {
            Products.deleteWhere("number = ?", "11111111").execute(recorder)
        }

        assertThat("records deleted", deleted, equalTo(1))
        val records = Products.selectAll(recorder) { product }

        assertThat("records left", records, hasSize(equalTo(2)))
        assertThat("remaining", records, anyElement(hasName(frenchie.name)) and anyElement(hasName(lab.name)))
    }

    private fun persist(product: Product) {
        transaction {
            Products.insert(product.record).execute(recorder)
        }
    }
}
