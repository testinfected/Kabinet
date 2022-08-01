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
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SelectionTest {

    val database = Database.inMemory()
    val connection = database.openConnection()
    val recorder = StatementRecorder(connection)

    val persisted = Persister(connection)

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
        val id = persisted(frenchie)

        val records = Products.selectAll(recorder) { it.product }

        assertThat("record", records, anyElement(hasSameStateAs(frenchie.copy(id = id))))

        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products")
    }

    val bully = Product(number = 12345678, name = "English Bulldog", description = "A heavy, muscular dog")
    val lab = Product(name = "Labrador Retriever", number = 33333333)

    @Test
    fun `selecting all records from a table`() {
        persisted(frenchie)
        persisted(bully)
        persisted(lab)

        val selection = Products.selectAll(recorder) { it.product }

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
        persisted(frenchie)
        persisted(bully)
        persisted(lab)

        val selection = Products.selectAll().firstOrNull(recorder) { it.product }

        assertThat("selected", selection, present(hasName("French Bulldog")))

        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products LIMIT 1")
    }

    val dalmatian = Product(name = "Dalmatian", number = 55555555)

    @Test
    fun `limiting the quantity of results`() {
        persisted(lab)
        persisted(frenchie)
        persisted(bully)
        persisted(dalmatian)

        val selection =
            Products
                .selectAll()
                .limit(2, offset = 1)
                .list(recorder) { it.product }

        assertThat("selected", selection, allElements(hasName(containsSubstring("Bulldog"))))
        assertThat("selection", selection, hasSize(equalTo(2)))

        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products LIMIT 2 OFFSET 1")
    }

    @Test
    fun `selecting only those records that fulfill a specified criterion`() {
        persisted(lab)
        persisted(frenchie)
        persisted(dalmatian)

        val selection =
            Products
                .selectWhere("name = ?", "French Bulldog")
                .list(recorder) { it.product }

        assertThat("selected", selection, anyElement(hasName(containsSubstring("Bulldog"))))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description FROM products " +
                    "WHERE name = 'French Bulldog'"
        )
    }

    @Test
    fun `selecting only a subset of the table columns`() {
        persisted(frenchie)

        val slices =
            Products
                .slice(number, name)
                .selectAll(recorder) { it[number] to it[name] }

        assertThat("slices", slices, hasElement(77777777 to "French Bulldog"))

        recorder.assertSql("SELECT products.number, products.name FROM products")
    }

    @Test
    fun `slicing using a literal expression`() {
        persisted(frenchie)
        persisted(lab)
        persisted(dalmatian)

        val count = intLiteral("count(*)")

        val expr =
            Products
                .select(count)
                .firstOrNull(recorder) { it[count] }

        assertThat("expr", expr, equalTo(3))

        recorder.assertSql("SELECT count(*) FROM products LIMIT 1")
    }

    @Test
    fun `aliasing the table name`() {
        persisted(frenchie)
        persisted(dalmatian)
        persisted(lab)

        val selection =
            Products
                .alias("p")
                .selectWhere("p.name = ?", "Labrador Retriever")
                .list(recorder) { it.product("p") }

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
        persisted(Item(productId = persisted(boxer), number = "543261", price = BigDecimal("1199.00")))
        persisted(Item(productId = persisted(lab), number = "917541", price = BigDecimal("799.00")))

        val selection =
            Products
                .join(Items, "products.id = items.product_id")
                .selectWhere("items.price > ?", BigDecimal("1000"))
                .list(recorder) { it.product }

        assertThat("selected", selection, anyElement(hasName("Boxer")))
        assertThat("selection", selection, hasSize(equalTo(1)))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description, items.id, items.number, items.product_id, items.price, items.on_sale " +
                    "FROM products INNER JOIN items ON products.id = items.product_id WHERE items.price > 1000"
        )
    }

    @Test
    fun `joining with another table, this time specifying join columns`() {
        persisted(Item(productId = persisted(boxer), number = "543261", price = BigDecimal("1199.00")))
        persisted(Item(productId = persisted(lab), number = "917541", price = BigDecimal("799.00")))

        val selection =
            Products
                .join(Items, Products.id, Items.productId)
                .selectWhere("items.price < ?", BigDecimal("1000"))
                .list(recorder) { it.product }

        assertThat("selected", selection, anyElement(hasName("Labrador Retriever")))
        assertThat("selection", selection, hasSize(equalTo(1)))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description, items.id, items.number, items.product_id, items.price, items.on_sale " +
                    "FROM products INNER JOIN items ON products.id = items.product_id WHERE items.price < 1000"
        )
    }

    @Test
    fun `joining with another table, using join columns and an additional constraint`() {
        persisted(boxer).also {
            persisted(Item(productId = it, number = "543261", price = BigDecimal("1199.00")))
            persisted(Item(productId = it, number = "666633", price = BigDecimal("999.00")))
        }
        persisted(Item(productId = persisted(lab), number = "917541", price = BigDecimal("799.00")))

        val selection =
            Products
                .join(Items, Products.id, Items.productId, "$name <> ?", boxer.name)
                .selectWhere("items.price < ?", BigDecimal("1000"))
                .list(recorder) { it.product }

        assertThat("selected", selection, anyElement(hasName("Labrador Retriever")))
        assertThat("selection", selection, hasSize(equalTo(1)))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description, items.id, items.number, items.product_id, items.price, items.on_sale " +
                    "FROM products INNER JOIN items ON products.id = items.product_id AND products.name <> 'Boxer' WHERE items.price < 1000"
        )
    }

    @Test
    fun `aliasing the join table`() {
        persisted(Item(productId = persisted(boxer), number = "543261", price = BigDecimal("1199.00")))
        persisted(Item(productId = persisted(lab), number = "917541", price = BigDecimal("799.00")))

        val selection =
            Products
                .join(Items.alias("item"), "products.id = item.product_id")
                .selectWhere("item.price > ?", BigDecimal("1000"))
                .list(recorder) { it.product }

        assertThat("selected", selection, anyElement(hasName("Boxer")))
        assertThat("selection", selection, hasSize(equalTo(1)))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description, item.id, item.number, item.product_id, item.price, item.on_sale " +
                    "FROM products INNER JOIN items AS item ON products.id = item.product_id WHERE item.price > 1000"
        )
    }

    @Test
    fun `retrieving only the joined table columns`() {
        persisted(Item(productId = persisted(boxer), number = "543261", price = BigDecimal("1199.00")))
        persisted(Item(productId = persisted(lab), number = "917541", price = BigDecimal("799.00")))

        val selection =
            Products
                .join(Items.alias("item"), "products.id = item.product_id")
                .slice(Products)
                .selectWhere("item.price > ?", BigDecimal("1000"))
                .list(recorder) { it.product }

        assertThat("selected", selection, anyElement(hasName("Boxer")))
        assertThat("selection", selection, hasSize(equalTo(1)))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description " +
                    "FROM products INNER JOIN items AS item ON products.id = item.product_id WHERE item.price > 1000"
        )
    }

    @Test
    fun `querying data from multiple tables using a left join`() {
        persisted(Order(number = 10000001))
        persisted(Order(number = 20000001))
        val paid = Order(number = 20000002).also {
            it.id = persisted(it)
        }

        persisted(paid.paymentAt(LocalDateTime.parse("2021-12-25T15:00")))

        val selection =
            Orders
                .leftJoin(Payments, Orders.id, Payments.orderId)
                .slice(Orders)
                .selectWhere("orders.number > ?", 20000000L)
                .list(recorder) { it.order }

        assertThat(
            "selected", selection, allOf(
                anyElement(hasNumber(20000001)),
                anyElement(hasNumber(20000002))
            )
        )
        assertThat("selection", selection, hasSize(equalTo(2)))

        recorder.assertSql(
            "SELECT orders.id, orders.number, orders.placed_at " +
                    "FROM orders LEFT JOIN payments ON orders.id = payments.order_id " +
                    "WHERE orders.number > 20000000"
        )
    }

    @Test
    fun `using multiple joins`() {
        with(persisted(boxer)) {
            persisted(Item(productId = this, number = "1001", price = BigDecimal("1199.00")))
        }
        with(persisted(frenchie)) {
            persisted(Item(productId = this, number = "2001", price = BigDecimal("4499.00")))
            persisted(Item(productId = this, number = "2002", price = BigDecimal("4499.00")))
        }
        with(persisted(lab)) {
            persisted(Item(productId = this, number = "3001", price = BigDecimal("799.00")))
            persisted(Item(productId = this, number = "3002", price = BigDecimal("899.00")))
        }

        val product = Products.alias("product")
        val item = Items.alias("item")
        val other = Items.alias("other")

        val selection = product
            .join(item, product[id], item[productId])
            .leftJoin(other, product[id], other[productId])
            .slice(product[name])
            .selectWhere("item.number <> other.number AND item.price = other.price")
            .listDistinct(recorder) { it[product[name]] }

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
        persisted(Order(number = 20000001))
        persisted(Order(number = 10000002))
        persisted(Order(number = 10000003))
        persisted(Order(number = 10000004))
        persisted(Order(number = 20000005))

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
        val best = persisted(frenchie)
        persisted(Item(productId = best, number = "0001"))
        persisted(Item(productId = best, number = "0002"))
        persisted(Item(productId = persisted(lab), number = "0003"))
        persisted(Item(productId = persisted(boxer), number = "0004"))

        val uniques = Products
            .join(Items, "items.product_id = products.id")
            .slice(Products)
            .selectAll()
            .listDistinct(recorder) { it.product }


        assertThat("distinct products", uniques, hasSize(equalTo(3)))

        recorder.assertSql(
            "SELECT DISTINCT products.id, products.number, products.name, products.description " +
                    "FROM products " +
                    "INNER JOIN items ON items.product_id = products.id"
        )
    }

    @Test
    fun `counting only distinct records`() {
        persisted(frenchie).also { id ->
            persisted(Item(productId = id, number = "0001", price = BigDecimal.valueOf(100)))
            persisted(Item(productId = id, number = "0002", price = BigDecimal.valueOf(100)))
        }
        persisted(Item(productId = persisted(lab), number = "0003", price = BigDecimal.valueOf(100)))
        persisted(Item(productId = persisted(boxer), number = "0004", price = BigDecimal.valueOf(100)))

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

    @Test
    fun `ordering extracted records, using a literal expression`() {
        val id = persisted(frenchie)
        val one = Item(productId = id, number = "0001", price = BigDecimal(3199))
        val two = Item(productId = id, number = "0002", price = BigDecimal(3299))
        val three = Item(productId = id, number = "0003", price = BigDecimal(3399))

        listOf(one, two, three).forEach { it.id = persisted(it)}

        val order = Order(number = 10000001) + one + two + three
        persisted(order).let { order.id = it }

        val numbers = LineItems
            .slice(LineItems.itemNumber)
            .selectWhere("order_id = ?", order.id)
            .orderBy("order_line DESC")
            .list(recorder) { it[LineItems.itemNumber] }

        assertThat(
            "ordered selection", numbers, equalTo(listOf("0003", "0002", "0001"))
        )

        recorder.assertSql(
            "SELECT line_items.item_number " +
                    "FROM line_items " +
                    "WHERE order_id = ${order.id} " +
                    "ORDER BY order_line DESC"
        )
    }


    @Test
    fun `ordering extracted records, using a column expression`() {
        val id = persisted(frenchie)
        val one = Item(productId = id, number = "0001", price = BigDecimal(3199))
        val two = Item(productId = id, number = "0002", price = BigDecimal(3299))
        val three = Item(productId = id, number = "0003", price = BigDecimal(3399))

        listOf(one, two, three).forEach { it.id = persisted(it)}

        val order = Order(number = 10000001) + one + two + three
        persisted(order).let { order.id = it }

        val selection = LineItems
            .slice(LineItems.itemNumber)
            .selectWhere("order_id = ?", order.id)
            .orderBy(LineItems.index, SortOrder.DESC)
            .list(recorder) { it[LineItems.itemNumber] }

        assertThat(
            "ordered selection", selection, equalTo(listOf("0003", "0002", "0001"))
        )

        recorder.assertSql(
            "SELECT line_items.item_number " +
                    "FROM line_items " +
                    "WHERE order_id = ${order.id} " +
                    "ORDER BY line_items.order_line DESC"
        )
    }
}
