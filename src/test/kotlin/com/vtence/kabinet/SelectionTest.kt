package com.vtence.kabinet

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import com.vtence.kabinet.ProductThat.hasName
import com.vtence.kabinet.ProductThat.hasSameStateAs
import com.vtence.kabinet.Products.name
import com.vtence.kabinet.Products.number
import java.math.BigDecimal
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SelectionTest {

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

    val frenchie = Product(number = 77777777, name = "French Bulldog", description = "A cute, family dog")

    @Test
    fun `retrieving a record from a table`() {
        val id = persist(frenchie)

        val records = Products.selectAll(recorder) { product }

        assertThat("record", records, anyElement(hasSameStateAs(frenchie.copy(id = id))))

        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products")
    }

    val bully = Product(number = 12345678, name = "English Bulldog", description = "A heavy, muscular dog")
    val lab = Product(name = "Labrador Retriever", number = 33333333)

    @Test
    fun `selecting all records from a table`() {
        persist(frenchie)
        persist(bully)
        persist(lab)

        val selection = Products.selectAll(recorder) { product }

        assertThat("selection", selection, hasSize(equalTo(3)))
        assertThat(
            "selected", selection,
            anyElement(hasName("French Bulldog")) and
                    anyElement(hasName("English Bulldog")) and
                    anyElement(
                        hasName("Labrador Retriever")
                    )
        )

        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products")
    }

    @Test
    fun `selecting the first in a collection of records`() {
        persist(frenchie)
        persist(bully)
        persist(lab)

        val selection = Products.selectAll().first(recorder) { product }

        assertThat("selected", selection, present(hasName("French Bulldog")))

        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products LIMIT 1")
    }

    val dalmatian = Product(name = "Dalmatian", number = 55555555)

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

        assertThat("selection", selection, hasSize(equalTo(2)))
        assertThat(
            "selected", selection, allElements(hasName(containsSubstring("Bulldog")))
        )

        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products LIMIT 2 OFFSET 1")
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

        assertThat(
            "selected", selection, anyElement(hasName(containsSubstring("Bulldog")))
        )

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description FROM products " +
                    "WHERE name = 'French Bulldog'"
        )
    }

    @Test
    fun `selecting only a subset of the table columns`() {
        persist(frenchie)

        val slices =
            Products
                .slice(number, name)
                .selectAll(recorder) { this[number] to this[name] }

        assertThat(
            "slices", slices, hasElement(77777777 to "French Bulldog")
        )

        recorder.assertSql("SELECT products.number, products.name FROM products")
    }

    @Test
    fun `slicing using a literal expression`() {
        persist(frenchie)
        persist(lab)
        persist(dalmatian)

        val count = intLiteral("count(*)")

        val expr =
            Products
                .slice(count)
                .selectFirst(recorder) { this[count] }

        assertThat("expr", expr, equalTo(3))

        recorder.assertSql("SELECT count(*) FROM products LIMIT 1")
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

        assertThat("selection", selection, hasSize(equalTo(1)))
        assertThat("selected", selection, anyElement(hasName("Labrador Retriever")))

        recorder.assertSql(
            "SELECT p.id, p.number, p.name, p.description " +
                    "FROM products AS p WHERE p.name = 'Labrador Retriever'"
        )
    }

    val boxer = Product(name = "Boxer", number = 88889999)

    @Test
    fun `joining with another table, using a literal expression`() {
        persist(Item(productId = persist(boxer), number = "543261", price = BigDecimal("1199.00")))
        persist(Item(productId = persist(lab), number = "917541", price = BigDecimal("799.00")))

        val selection =
            Products
                .join(Items, "products.id = items.product_id")
                .selectWhere("items.price > ?", BigDecimal("1000"))
                .list(recorder) { product }

        assertThat("selection", selection, hasSize(equalTo(1)))
        assertThat("selected", selection, anyElement(hasName("Boxer")))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description, items.id, items.number, items.product_id, items.price " +
                    "FROM products JOIN items ON products.id = items.product_id WHERE items.price > 1000")
    }

    @Test
    fun `joining with another table, this time specifying join columns`() {
        persist(Item(productId = persist(boxer), number = "543261", price = BigDecimal("1199.00")))
        persist(Item(productId = persist(lab), number = "917541", price = BigDecimal("799.00")))

        val selection =
            Products
                .join(Items, Products.id, Items.productId)
                .selectWhere("items.price < ?", BigDecimal("1000"))
                .list(recorder) { product }

        assertThat("selection", selection, hasSize(equalTo(1)))
        assertThat("selected", selection, anyElement(hasName("Labrador Retriever")))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description, items.id, items.number, items.product_id, items.price " +
                    "FROM products JOIN items ON products.id = items.product_id WHERE items.price < 1000")
    }

    @Test
    fun `aliasing the join table`() {
        persist(Item(productId = persist(boxer), number = "543261", price = BigDecimal("1199.00")))
        persist(Item(productId = persist(lab), number = "917541", price = BigDecimal("799.00")))

        val selection =
            Products
                .join(Items.alias("item"), "products.id = item.product_id")
                .selectWhere("item.price > ?", BigDecimal("1000"))
                .list(recorder) { product }

        assertThat("selection", selection, hasSize(equalTo(1)))
        assertThat("selected", selection, anyElement(hasName("Boxer")))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description, item.id, item.number, item.product_id, item.price " +
                    "FROM products JOIN items AS item ON products.id = item.product_id WHERE item.price > 1000")
    }

    @Test
    fun `retrieving only the joined table columns`() {
        persist(Item(productId = persist(boxer), number = "543261", price = BigDecimal("1199.00")))
        persist(Item(productId = persist(lab), number = "917541", price = BigDecimal("799.00")))

        val selection =
            Products
                .join(Items.alias("item"), "products.id = item.product_id")
                .slice(Products)
                .selectWhere("item.price > ?", BigDecimal("1000"))
                .list(recorder) { product }

        assertThat("selection", selection, hasSize(equalTo(1)))
        assertThat("selected", selection, anyElement(hasName("Boxer")))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description " +
                    "FROM products JOIN items AS item ON products.id = item.product_id WHERE item.price > 1000")
    }

    private fun persist(product: Product): Int {
        return transaction {
            Products.insert(product.record).execute(recorder) get Products.id
        }
    }

    private fun persist(item: Item): Int {
        return transaction {
            Items.insert(item.record).execute(recorder) get Items.id
        }
    }
}
