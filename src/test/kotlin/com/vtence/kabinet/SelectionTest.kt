package com.vtence.kabinet

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import com.vtence.kabinet.Items.productId
import com.vtence.kabinet.OrderThat.hasNumber
import com.vtence.kabinet.ProductThat.hasName
import com.vtence.kabinet.ProductThat.hasSameStateAs
import com.vtence.kabinet.Products.id
import com.vtence.kabinet.Products.name
import com.vtence.kabinet.Products.number
import java.math.BigDecimal
import java.time.LocalDate
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

        val selection = Products.selectAll().firstOrNull(recorder) { product }

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

        assertThat("selected", selection, allElements(hasName(containsSubstring("Bulldog"))))
        assertThat("selection", selection, hasSize(equalTo(2)))

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

        assertThat("selected", selection, anyElement(hasName(containsSubstring("Bulldog"))))

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

        assertThat("slices", slices, hasElement(77777777 to "French Bulldog"))

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
                .selectAll()
                .firstOrNull(recorder) { this[count] }

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

        assertThat("selected", selection, anyElement(hasName("Labrador Retriever")))
        assertThat("selection", selection, hasSize(equalTo(1)))

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

        assertThat("selected", selection, anyElement(hasName("Boxer")))
        assertThat("selection", selection, hasSize(equalTo(1)))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description, items.id, items.number, items.product_id, items.price " +
                    "FROM products INNER JOIN items ON products.id = items.product_id WHERE items.price > 1000"
        )
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

        assertThat("selected", selection, anyElement(hasName("Labrador Retriever")))
        assertThat("selection", selection, hasSize(equalTo(1)))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description, items.id, items.number, items.product_id, items.price " +
                    "FROM products INNER JOIN items ON products.id = items.product_id WHERE items.price < 1000"
        )
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

        assertThat("selected", selection, anyElement(hasName("Boxer")))
        assertThat("selection", selection, hasSize(equalTo(1)))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description, item.id, item.number, item.product_id, item.price " +
                    "FROM products INNER JOIN items AS item ON products.id = item.product_id WHERE item.price > 1000"
        )
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

        assertThat("selected", selection, anyElement(hasName("Boxer")))
        assertThat("selection", selection, hasSize(equalTo(1)))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description " +
                    "FROM products INNER JOIN items AS item ON products.id = item.product_id WHERE item.price > 1000"
        )
    }

    @Test
    fun `querying data from multiple tables using a left join`() {
        persist(Order(number = "00000001"))
        persist(Order(number = "10000001"))
        val paid = Order(number = "10000002").also {
            it.id = persist(it)
        }

        persist(paid.paymentOn(LocalDate.parse("2021-12-25")))

        val selection =
            Orders
                .leftJoin(Payments, Orders.id, Payments.orderId)
                .slice(Orders)
                .selectWhere("orders.number LIKE ?", "1000%")
                .list(recorder) { order }

        assertThat(
            "selected", selection, allOf(
                anyElement(hasNumber("10000001")),
                anyElement(hasNumber("10000002"))
            )
        )
        assertThat("selection", selection, hasSize(equalTo(2)))

        recorder.assertSql(
            "SELECT orders.id, orders.number, orders.placed_at " +
                    "FROM orders LEFT JOIN payments ON orders.id = payments.order_id " +
                    "WHERE orders.number LIKE '1000%'"
        )
    }

    @Test
    fun `using multiple joins`() {
        with(persist(boxer)) {
            persist(Item(productId = this, number = "1001", price = BigDecimal("1199.00")))
        }
        with(persist(frenchie)) {
            persist(Item(productId = this, number = "2001", price = BigDecimal("4499.00")))
            persist(Item(productId = this, number = "2002", price = BigDecimal("4499.00")))
        }
        with(persist(lab)) {
            persist(Item(productId = this, number = "3001", price = BigDecimal("799.00")))
            persist(Item(productId = this, number = "3002", price = BigDecimal("899.00")))
        }

        val product = Products.alias("product")
        val item = Items.alias("item")
        val other = Items.alias("other")

        val selection = product
            .join(item, product[id], item[productId])
            .leftJoin(other, product[id], other[productId])
            .slice(product[name])
            .selectWhere("item.number <> other.number AND item.price = other.price")
            .listDistinct(recorder) { get(product[name]) }

        assertThat("selection", selection, hasSize(equalTo(1)))
        assertThat("selection", selection, anyElement(equalTo("French Bulldog")))

        recorder.assertSql(
            "SELECT DISTINCT product.name " +
                    "FROM products AS product " +
                    "INNER JOIN items AS item ON product.id = item.product_id " +
                    "LEFT JOIN items AS other ON product.id = other.product_id " +
                    "WHERE item.number <> other.number AND item.price = other.price"
        )
    }

    @Test
    fun `counting the number of records`() {
        persist(Order(number = "00000001"))
        persist(Order(number = "10000002"))
        persist(Order(number = "10000003"))
        persist(Order(number = "10000004"))
        persist(Order(number = "00000005"))

        val count =
            Orders
                .selectWhere("number like '1%'")
                .count(recorder)

        assertThat("total orders", count, equalTo(3))

        recorder.assertSql(
            "SELECT COUNT(*) FROM orders WHERE number like '1%'"
        )
    }

    @Test
    fun `selecting only distinct records`() {
        val best = persist(frenchie)
        persist(Item(productId = best, number = "0001", price = BigDecimal.valueOf(100)))
        persist(Item(productId = best, number = "0002", price = BigDecimal.valueOf(100)))
        persist(Item(productId = persist(lab), number = "0003", price = BigDecimal.valueOf(100)))
        persist(Item(productId = persist(boxer), number = "0004", price = BigDecimal.valueOf(100)))

        val uniques = Products
            .join(Items, "items.product_id = products.id")
            .slice(Products)
            .selectAll()
            .listDistinct(recorder) { product }


        assertThat("distinct products", uniques, hasSize(equalTo(3)))

        recorder.assertSql(
            "SELECT DISTINCT products.id, products.number, products.name, products.description " +
                    "FROM products " +
                    "INNER JOIN items ON items.product_id = products.id"
        )
    }

    @Test
    fun `counting only distinct records`() {
        val best = persist(frenchie)
        persist(Item(productId = best, number = "0001", price = BigDecimal.valueOf(100)))
        persist(Item(productId = best, number = "0002", price = BigDecimal.valueOf(100)))
        persist(Item(productId = persist(lab), number = "0003", price = BigDecimal.valueOf(100)))
        persist(Item(productId = persist(boxer), number = "0004", price = BigDecimal.valueOf(100)))

        val count = Products
            .join(Items, "items.product_id = products.id")
            .slice(Products)
            .selectAll()
            .countDistinct(recorder)

        assertThat("count", count, equalTo(3))

        recorder.assertSql(
            "SELECT COUNT(DISTINCT (products.id, products.number, products.name, products.description)) " +
                    "FROM products " +
                    "INNER JOIN items ON items.product_id = products.id"
        )
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

    private fun persist(order: Order): Int {
        return transaction {
            Orders.insert(order.record).execute(recorder) get Orders.id
        }
    }

    private fun persist(payment: Payment): Int {
        return transaction {
            Payments.insert(payment.record).execute(recorder) get Payments.id
        }
    }
}
