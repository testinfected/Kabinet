package com.vtence.kabinet

import Product
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.anyElement
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.present
import com.vtence.kabinet.ProductThat.hasDescription
import com.vtence.kabinet.ProductThat.hasId
import com.vtence.kabinet.ProductThat.hasName
import com.vtence.kabinet.ProductThat.hasNumber
import org.junit.jupiter.api.Test
import java.sql.ResultSet
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class PlainSqlTest {

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
    fun `inserting and retrieving a single database row with all columns`() {
        val id = transaction {
            val bulldog = sql(
                "INSERT INTO products(number, name, description) VALUES(:number, :name, :description)"
            )
            bulldog
                .set("number", "12345678")
                .set("name", "English Bulldog")
                .set("description", "A muscular, heavy dog")
                .insert(recorder) { id }
        }

        recorder.assertSql(
            "INSERT INTO products(number, name, description) VALUES('12345678', 'English Bulldog', 'A muscular, heavy dog')")
        assertThat("id", id, present())

        val products = sql(
            "SELECT * FROM products WHERE number = :number"
        )
        val found = products.set("number", "12345678").list(recorder) {
            Product(
                id = getInt("id"),
                number = getInt("number"),
                name = getString("name"),
                description = getString("description"),
            )
        }

        recorder.assertSql("SELECT * FROM products WHERE number = '12345678'")
        assertThat(
            "products found", found, anyElement(
                hasId(id) and
                        hasNumber(12345678) and
                        hasName("English Bulldog") and
                        hasDescription("A muscular, heavy dog")
            )
        )
    }

    val ResultSet.id: Int get() {
        return getInt(runCatching { findColumn("id") }.getOrDefault(1))
    }
}
