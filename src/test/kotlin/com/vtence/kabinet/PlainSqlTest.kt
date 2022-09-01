package com.vtence.kabinet

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
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
                .set("description", null)
                .insert(recorder) { it.id }
        }

        recorder.assertSql(
            "INSERT INTO products(number, name, description) VALUES('12345678', 'English Bulldog', NULL)"
        )

        assertThat("id", id, present())

        val dog = sql(
            "SELECT * FROM products WHERE number = :number"
        )
        val found = dog.set("number", "12345678").firstOrNull(recorder) { rs ->
            Product(
                id = rs.getInt("id"),
                number = rs.getInt("number"),
                name = rs.getString("name"),
                description = rs.getString("description"),
            )
        }

        recorder.assertSql("SELECT * FROM products WHERE number = '12345678'")
        assertThat(
            "dogs found", found, present(
                hasId(id) and
                        hasNumber(12345678) and
                        hasName("English Bulldog") and
                        hasDescription(null)
            )
        )
    }

    @Test
    fun `selecting many rows`() {
        fun dog() = sql(
            "INSERT INTO products(number, name, description) VALUES(:number, :name, :description)"
        )

        transaction {
            dog()
                .set("number", "111111111")
                .set("name", "English Bulldog")
                .set("description", null)
                .insert(recorder)

            dog()
                .set("number", "22222222")
                .set("name", "Labrador Retriever")
                .set("description", "Chocolate")
                .insert(recorder)
        }

        val dogs = sql("SELECT * FROM products")

        val found = dogs.list(recorder) { rs ->
            Product(
                id = rs.getInt("id"),
                number = rs.getInt("number"),
                name = rs.getString("name"),
                description = rs.getString("description"),
            )
        }

        recorder.assertSql("SELECT * FROM products")
        assertThat("found", found, hasSize(equalTo(2)))
    }

    @Test
    fun `updating a row`() {
        fun dog() = sql(
            "INSERT INTO products(number, name, description) VALUES(:number, :name, :description)"
        )

        transaction {
            dog()
                .set("number", "77777777")
                .set("name", "French Bulldog")
                .set("description", null)
                .insert(recorder)
        }

        val dogs = sql("UPDATE products SET description = :description")
            .set("description", "An adorable loving dog")


        val updated = dogs.execute(recorder)

        recorder.assertSql("UPDATE products SET description = 'An adorable loving dog'")
        assertThat("updated", updated, equalTo(1))
    }


    @Test
    fun `deleting many rows`() {
        fun dog() = sql(
            "INSERT INTO products(number, name, description) VALUES(:number, :name, :description)"
        )

        transaction {
            dog()
                .set("number", "111111111")
                .set("name", "English Bulldog")
                .set("description", null)
                .insert(recorder)

            dog()
                .set("number", "22222222")
                .set("name", "Labrador Retriever")
                .set("description", "Chocolate")
                .insert(recorder)
        }

        val dogs = sql("DELETE FROM products")

        val deleted = dogs.execute(recorder)

        recorder.assertSql("DELETE FROM products")
        assertThat("deleted", deleted, equalTo(2))
    }

    val ResultSet.id: Int
        get() = getInt(runCatching { findColumn("id") }.getOrDefault(1))
}
