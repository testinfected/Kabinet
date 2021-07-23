package com.vtence.kabinet

import Product
import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.present
import com.vtence.kabinet.ProductThat.hasDescription
import com.vtence.kabinet.ProductThat.hasId
import com.vtence.kabinet.ProductThat.hasName
import com.vtence.kabinet.ProductThat.hasNumber
import com.vtence.kabinet.ProductThat.hasSameStateAs
import com.vtence.kabinet.Products.id
import kotlin.test.*

class InsertionTest {
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
    fun `inserting a new record`() {
        transaction {
            Products.insert {
                it[number] = 12345678
                it[description] = "A muscular, heavy dog"
                it[name] = "English Bulldog"
            }.execute(recorder)
        }
        recorder.assertSql(
            "INSERT INTO products(number, name, description) " +
                    "VALUES(12345678, 'English Bulldog', 'A muscular, heavy dog')"
        )

        val inserted = Products.selectAll(recorder) { product }.singleOrNull()
        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products")

        assertThat(
            "inserted", inserted, present(
                hasNumber(12345678) and
                        hasName("English Bulldog") and
                        hasDescription("A muscular, heavy dog")
            )
        )
    }

    @Test
    fun `retrieving the generated keys`() {
        val id = transaction {
            Products.insert {
                it[number] = 12345678
                it[description] = "A muscular, heavy dog"
                it[name] = "English Bulldog"
            }.execute(recorder) get id
        }
        recorder.assertSql(
            "INSERT INTO products(number, name, description) " +
                    "VALUES(12345678, 'English Bulldog', 'A muscular, heavy dog')"
        )

        val inserted = Products.selectAll(recorder) { product }.singleOrNull()
        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products")

        assertThat("inserted", inserted, present(hasId(id)))
    }

    val bulldog = Product(number = 12345678, name = "English Bulldog", description = "A muscular, heavy dog")

    @Test
    fun `inserting again, this time using a record definition`() {
        val id = transaction {
            Products.insert(bulldog.record).execute(recorder) get id
        }
        recorder.assertSql(
            "INSERT INTO products(number, name, description) " +
                    "VALUES(12345678, 'English Bulldog', 'A muscular, heavy dog')"
        )

        val inserted = Products.selectAll(recorder) { product }.singleOrNull()
        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products")

        assertThat("inserted", inserted, present(hasSameStateAs(bulldog.copy(id = id))))
    }

    @Test
    fun `omitting nullable columns`() {
        val id = transaction {
            Products.insert {
                it[number] = 77777777
                it[name] = "French Bulldog"
            }.execute(recorder) get id
        }
        recorder.assertSql(
            "INSERT INTO products(number, name, description) " +
                    "VALUES(77777777, 'French Bulldog', NULL)")

        val inserted = Products.selectAll(recorder) { product }.singleOrNull()
        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products")

        assertThat(
            "inserted", inserted,
            present(
                hasId(id) and
                        hasNumber(77777777) and
                        hasName("French Bulldog") and
                        hasDescription(absent())
            )
        )
    }
}
