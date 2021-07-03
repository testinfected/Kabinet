package com.vtence.kabinet

import Product
import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import com.vtence.kabinet.ProductThat.hasName
import com.vtence.kabinet.ProductThat.hasSameStateAs
import com.vtence.kabinet.Products.id
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SelectionTest {

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

    val frenchie = Product(number = "77777777", name = "French Bulldog", description = "A cute, family dog")

    @Test
    fun `retrieving a record from a table`() {
        val id = transaction {
            Products.insert(frenchie.record).execute(connection) get id
        }

        val records = Products.selectAll().list(connection) { Products.hydrate(this) }

        assertThat("record", records, anyElement(hasSameStateAs(frenchie.copy(id = id))))
    }

    val bully = Product(number = "12345678", name = "English Bulldog", description = "A heavy, muscular dog")
    val lab = Product(name = "Labrador retriever", number = "33333333")

    @Test
    fun `selecting all records from a table`() {
        persist(frenchie)
        persist(bully)
        persist(lab)

        val selection = Products.selectAll().list(connection) { Products.hydrate(this) }

        assertThat("selection", selection, hasSize(equalTo(3)))
        assertThat(
            "selected", selection,
            anyElement(hasName("French Bulldog")) and
                    anyElement(hasName("English Bulldog")) and
                    anyElement(
                        hasName("Labrador retriever")
                    )
        )
    }

    @Test
    fun `selecting the first in a collection of records`() {
        persist(frenchie)
        persist(bully)
        persist(lab)

        val selection = Products.selectAll().single(connection) { Products.hydrate(this) }

        assertThat("selected", selection, present(hasName("French Bulldog")))
    }

    val dalmatian = Product(name = "Dalmatian", number = "55555555")

    @Test
    fun `limiting the results to a subset`() {
        persist(lab)
        persist(frenchie)
        persist(bully)
        persist(dalmatian)

        val selection = Products.selectAll()
            .limit(2, offset = 1)
            .list(connection) { Products.hydrate(this) }

        assertThat("selection", selection, hasSize(equalTo(2)))
        assertThat(
            "selected", selection, allElements(hasName(containsSubstring("Bulldog")))
        )
    }

    @Test
    fun `selecting only those records that fulfill a specified criterion`() {
        persist(lab)
        persist(frenchie)
        persist(dalmatian)

        val selection = Products.selectWhere("name = ?", "French Bulldog")
            .list(connection) { Products.hydrate(this) }

        assertThat(
            "selected", selection, anyElement(hasName(containsSubstring("Bulldog")))
        )
    }

    private fun persist(product: Product): Int {
        return transaction {
            Products.insert(product.record).execute(connection) get id
        }
    }
}
