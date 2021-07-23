package com.vtence.kabinet

import Product
import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import com.vtence.kabinet.ProductThat.hasName
import com.vtence.kabinet.ProductThat.hasSameStateAs
import com.vtence.kabinet.Products.id
import com.vtence.kabinet.Products.name
import com.vtence.kabinet.Products.number
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SelectionTest {

    val database = Database.inMemory()
    val connection = database.openConnection()
    val transaction = JDBCTransactor(connection)

    val recorder = StatementRecorder(connection)

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
        val id = persist(frenchie)

        val records = Products.selectAll(recorder) { product }
        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products")

        assertThat("record", records, anyElement(hasSameStateAs(frenchie.copy(id = id))))
    }

    val bully = Product(number = "12345678", name = "English Bulldog", description = "A heavy, muscular dog")
    val lab = Product(name = "Labrador Retriever", number = "33333333")

    @Test
    fun `selecting all records from a table`() {
        persist(frenchie)
        persist(bully)
        persist(lab)

        val selection = Products.selectAll(recorder) { product }
        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products")

        assertThat("selection", selection, hasSize(equalTo(3)))
        assertThat(
            "selected", selection,
            anyElement(hasName("French Bulldog")) and
                    anyElement(hasName("English Bulldog")) and
                    anyElement(
                        hasName("Labrador Retriever")
                    )
        )
    }

    @Test
    fun `selecting the first in a collection of records`() {
        persist(frenchie)
        persist(bully)
        persist(lab)

        val selection = Products.selectAll().first(recorder) { product }
        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products LIMIT 1")

        assertThat("selected", selection, present(hasName("French Bulldog")))
    }

    val dalmatian = Product(name = "Dalmatian", number = "55555555")

    @Test
    fun `limiting the quantity of results`() {
        persist(lab)
        persist(frenchie)
        persist(bully)
        persist(dalmatian)

        val selection =
            Products
                .selectAll()
                .limit(2, offset = 1)
                .list(recorder) { product }
        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products LIMIT 2 OFFSET 1")

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

        val selection =
            Products
                .selectWhere("name = ?", "French Bulldog")
                .list(recorder) { product }
        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description FROM products " +
                    "WHERE name = 'French Bulldog'"
        )

        assertThat(
            "selected", selection, anyElement(hasName(containsSubstring("Bulldog")))
        )
    }

    @Test
    fun `selecting only a subset of the table columns`() {
        persist(frenchie)

        val slices =
            Products
                .slice(number, name)
                .selectAll(recorder) { this[number] to this[name] }

        recorder.assertSql(
            "SELECT products.number, products.name FROM products"
        )

        assertThat(
            "slices", slices, hasElement("77777777" to "French Bulldog")
        )
    }

    @Test
    fun `slicing using a literal expression`() {
        persist(frenchie)
        persist(lab)
        persist(dalmatian)

        val count = Literal("count(*)", IntColumnType)

        val expr =
            Products
                .slice(count)
                .selectFirst(recorder) { this[count] }

        recorder.assertSql("SELECT count(*) FROM products LIMIT 1")

        assertThat("expr", expr, equalTo(3))
    }

    @Test
    fun `aliasing the table name`() {
        persist(frenchie)
        persist(dalmatian)
        persist(lab)

        val selection =
            Products
                .alias("p")
                .selectWhere("p.name = ?", "Labrador Retriever")
                .list(recorder) { product("p") }

        recorder.assertSql(
            "SELECT p.id, p.number, p.name, p.description " +
                    "FROM products AS p WHERE p.name = 'Labrador Retriever'"
        )

        assertThat("selection", selection, hasSize(equalTo(1)))
        assertThat("selected", selection, anyElement(hasName("Labrador Retriever")))
    }

    private fun persist(product: Product): Int {
        return transaction {
            Products.insert(product.record).execute(recorder) get id
        }
    }
}
