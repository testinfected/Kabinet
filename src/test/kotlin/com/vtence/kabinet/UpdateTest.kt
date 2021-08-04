package com.vtence.kabinet

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
    fun `updating all records partially`() {
        persist(Product(number = 11111111, name = "English Bulldog"))
        persist(Product(number = 77777777, name = "French Bulldog"))

        val updated: Int = transaction {
            Products.update {
                it[description] = "A companion for kids"
            }.execute(recorder)
        }

        assertThat("update count", updated, equalTo(2))
        recorder.assertSql("UPDATE products SET description = 'A companion for kids'")

        val records = Products.selectAll(recorder) { product }

        assertThat("updated products", records, allElements(hasDescription("A companion for kids")))
        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products")
    }

    @Test
    fun `updating a specific existing record`() {
        persist(Product(number = 11111111, name = "English Bulldog"))
        persist(Product(number = 77777777, name = "French Bulldog"))

        transaction {
            val updated: Int = Products.updateWhere("$number = ?", 77777777) {
                it[name] = "Frenchie"
                it[description] = "A miniature Bulldog"
            }.execute(recorder)

            assertThat("update count", updated, equalTo(1))
            recorder.assertSql("UPDATE products SET name = 'Frenchie', description = 'A miniature Bulldog' WHERE products.number = 77777777")
        }

        val records = Products.selectAll(recorder) { product }

        assertThat(
            "updated record", records, anyElement(
                hasDescription("A miniature Bulldog") and hasNumber(77777777)
            )
        )
        assertThat(
            "original record", records, anyElement(
                hasDescription(absent()) and hasNumber(11111111)
            )
        )

        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products")
    }

    private fun persist(product: Product) {
        transaction {
            Products.insert(product.record).execute(recorder)
        }
    }
}
