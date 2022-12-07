package com.vtence.kabinet

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import com.vtence.kabinet.Items.productId
import com.vtence.kabinet.OrderThat.hasNumber
import com.vtence.kabinet.ProductThat.hasName
import com.vtence.kabinet.ProductThat.hasSameStateAs
import com.vtence.kabinet.Products.description
import com.vtence.kabinet.Products.id
import com.vtence.kabinet.Products.name
import com.vtence.kabinet.Products.number
import com.vtence.kabinet.SortOption.NULLS_FIRST
import com.vtence.kabinet.SortDirection.DESC
import java.math.BigDecimal
import java.time.LocalDateTime
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

        assertThat(records, anyElement(hasSameStateAs(frenchie.copy(id = id))))

        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products")
    }

    val bully = Product(number = 12345678, name = "English Bulldog", description = "A heavy, muscular dog")
    val lab = Product(name = "Labrador Retriever", number = 33333333)

    @Test
    fun `selecting all records from a table`() {
        persisted(frenchie)
        persisted(bully)
        persisted(lab)

        val records = Products.selectAll(recorder) { it.product }

        assertThat(records,
            anyElement(hasName("French Bulldog")) and
                    anyElement(hasName("English Bulldog")) and
                    anyElement(hasName("Labrador Retriever"))
        )
        assertThat(records, hasSize(equalTo(3)))

        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products")
    }

    @Test
    fun `selecting the first in a collection of records`() {
        persisted(frenchie)
        persisted(bully)
        persisted(lab)

        val record = Products.selectAll().firstOrNull(recorder) { it.product }

        assertThat(record, present(hasName("French Bulldog")))

        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products LIMIT 1")
    }

    val dalmatian = Product(name = "Dalmatian", number = 55555555)

    @Test
    fun `limiting the quantity of results`() {
        persisted(lab)
        persisted(frenchie)
        persisted(bully)
        persisted(dalmatian)

        val records =
            Products
                .selectAll()
                .limit(2, offset = 1)
                .list(recorder) { it.product }

        assertThat(records, allElements(hasName(containsSubstring("Bulldog"))))
        assertThat(records, hasSize(equalTo(2)))

        recorder.assertSql("SELECT products.id, products.number, products.name, products.description FROM products LIMIT 2 OFFSET 1")
    }

    @Test
    fun `selecting only those records that fulfill a specified criterion`() {
        persisted(lab)
        persisted(frenchie)
        persisted(dalmatian)

        val records =
            Products
                .selectWhere("name = ?", "French Bulldog")
                .list(recorder) { it.product }

        assertThat(records, anyElement(hasName(containsSubstring("Bulldog"))))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description FROM products " +
                    "WHERE name = 'French Bulldog'"
        )
    }

    @Test
    fun `selecting only a subset of the table columns`() {
        persisted(frenchie)

        val records =
            Products
                .slice(number, name)
                .selectAll(recorder) { it[number] to it[name] }

        assertThat(records, hasElement(77777777 to "French Bulldog"))

        recorder.assertSql("SELECT products.number, products.name FROM products")
    }

    @Test
    fun `slicing using a literal expression`() {
        persisted(frenchie)
        persisted(lab)
        persisted(dalmatian)

        val count = intLiteral("count(*)")

        val total =
            Products
                .select(count)
                .firstOrNull(recorder) { it[count] }

        assertThat(total, equalTo(3))

        recorder.assertSql("SELECT count(*) FROM products LIMIT 1")
    }

    @Test
    fun `aliasing the table name`() {
        persisted(frenchie)
        persisted(dalmatian)
        persisted(lab)

        val records =
            Products
                .alias("p")
                .selectWhere("p.name = ?", "Labrador Retriever")
                .list(recorder) { it.product("p") }

        assertThat(records, anyElement(hasName("Labrador Retriever")))
        assertThat(records, hasSize(equalTo(1)))

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

        val records =
            Products
                .join(Items, "products.id = items.product_id")
                .selectWhere("items.price > ?", BigDecimal("1000"))
                .list(recorder) { it.product }

        assertThat(records, anyElement(hasName("Boxer")))
        assertThat(records, hasSize(equalTo(1)))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description, items.id, items.number, items.product_id, items.price, items.on_sale " +
                    "FROM products INNER JOIN items ON products.id = items.product_id WHERE items.price > 1000"
        )
    }

    @Test
    fun `joining with another table, this time specifying join columns`() {
        persisted(Item(productId = persisted(boxer), number = "543261", price = BigDecimal("1199.00")))
        persisted(Item(productId = persisted(lab), number = "917541", price = BigDecimal("799.00")))

        val records =
            Products
                .join(Items, id, productId)
                .selectWhere("items.price < ?", BigDecimal("1000"))
                .list(recorder) { it.product }

        assertThat(records, anyElement(hasName("Labrador Retriever")))
        assertThat(records, hasSize(equalTo(1)))

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

        val records =
            Products
                .join(Items, id, productId, "$name <> ?", boxer.name)
                .selectWhere("items.price < ?", BigDecimal("1000"))
                .list(recorder) { it.product }

        assertThat(records, anyElement(hasName("Labrador Retriever")))
        assertThat(records, hasSize(equalTo(1)))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description, items.id, items.number, items.product_id, items.price, items.on_sale " +
                    "FROM products INNER JOIN items ON products.id = items.product_id AND products.name <> 'Boxer' WHERE items.price < 1000"
        )
    }

    @Test
    fun `aliasing the join table`() {
        persisted(Item(productId = persisted(boxer), number = "543261", price = BigDecimal("1199.00")))
        persisted(Item(productId = persisted(lab), number = "917541", price = BigDecimal("799.00")))

        val records =
            Products
                .join(Items.alias("item"), "products.id = item.product_id")
                .selectWhere("item.price > ?", BigDecimal("1000"))
                .list(recorder) { it.product }

        assertThat(records, anyElement(hasName("Boxer")))
        assertThat(records, hasSize(equalTo(1)))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description, item.id, item.number, item.product_id, item.price, item.on_sale " +
                    "FROM products INNER JOIN items AS item ON products.id = item.product_id WHERE item.price > 1000"
        )
    }

    @Test
    fun `retrieving only the source table columns`() {
        persisted(Item(productId = persisted(boxer), number = "543261", price = BigDecimal("1199.00")))
        persisted(Item(productId = persisted(lab), number = "917541", price = BigDecimal("799.00")))

        val records =
            Products
                .join(Items.alias("item"), "products.id = item.product_id")
                .slice(Products)
                .selectWhere("item.price > ?", BigDecimal("1000"))
                .list(recorder) { it.product }

        assertThat(records, anyElement(hasName("Boxer")))
        assertThat(records, hasSize(equalTo(1)))

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

        val records =
            Orders
                .leftJoin(Payments, Orders.id, Payments.orderId)
                .slice(Orders)
                .selectWhere("payments.amount IS NOT NULL")
                .list(recorder) { it.order }

        assertThat(records, allElements(hasNumber(20000002)))
        assertThat(records, hasSize(equalTo(1)))

        recorder.assertSql(
            "SELECT orders.id, orders.number, orders.placed_at " +
                    "FROM orders LEFT JOIN payments ON orders.id = payments.order_id " +
                    "WHERE payments.amount IS NOT NULL"
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

        val records = product
            .join(item, product[id], item[productId])
            .leftJoin(other, product[id], other[productId])
            .slice(product[name])
            .selectWhere("item.number <> other.number AND item.price = other.price")
            .listDistinct(recorder) { it[product[name]] }

        assertThat(records, allElements(equalTo("French Bulldog")))
        assertThat(records, hasSize(equalTo(1)))

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

        assertThat(count, equalTo(3))

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


        assertThat("total distinct products", uniques, hasSize(equalTo(3)))

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

        assertThat("total count", count, equalTo(3))

        recorder.assertSql(
            "SELECT COUNT(DISTINCT (products.id, products.number, products.name, products.description)) " +
                    "FROM products " +
                    "INNER JOIN items ON items.product_id = products.id"
        )
    }

    @Test
    fun `ordering records using a column expression`() {
        persisted(frenchie).let { id ->
            persisted(Item(productId = id, number = "0001", price = BigDecimal(3199)))
            persisted(Item(productId = id, number = "0002", price = BigDecimal(3299)))
            persisted(Item(productId = id, number = "0003", price = BigDecimal(3399)))
        }

        val numbers = Items
            .slice(Items.number)
            .selectAll()
            .orderBy(Items.number, DESC)
            .list(recorder) { it[Items.number] }

        assertThat(
            "ordered records", numbers, equalTo(listOf("0003", "0002", "0001"))
        )

        recorder.assertSql(
            "SELECT items.number " +
            "FROM items " +
            "ORDER BY items.number DESC"
        )
    }

    @Test
    fun `ordering records using a literal expression`() {
        persisted(frenchie).let { id ->
            persisted(Item(productId = id, number = "0001", price = BigDecimal(3199)))
            persisted(Item(productId = id, number = "0002", price = BigDecimal(3299)))
            persisted(Item(productId = id, number = "0003", price = BigDecimal(3399)))
        }

        val numbers = Items
            .slice(Items.number)
            .selectAll()
            .orderBy("substring(number from ? for ?)".asExpression<Nothing>(4, 1), DESC)
            .list(recorder) { it[Items.number] }

        assertThat(
            "ordered selection", numbers, equalTo(listOf("0003", "0002", "0001"))
        )

        recorder.assertSql(
            "SELECT items.number " +
                    "FROM items " +
                    "ORDER BY substring(number from 4 for 1) DESC"
        )
    }

    @Test
    fun `ordering records using a sort option`() {
        persisted(frenchie).let { id ->
            persisted(Item(productId = id, number = "0001", price = BigDecimal(3199.00)))
            persisted(Item(productId = id, number = "0002", price = BigDecimal(3299.00)))
            persisted(Item(productId = id, number = "0003", price = BigDecimal(3299.00)))
            persisted(Item(productId = id, number = "0004", price = null))
        }

        val items = Items
            .slice(Items.price, Items.number)
            .selectAll()
            .orderBy(Items.price to DESC(NULLS_FIRST), Items.number to DESC)
            .list(recorder) { it[Items.number] }

        assertThat(items, equalTo(listOf("0004", "0003", "0002", "0001")))

        recorder.assertSql(
            "SELECT items.price, items.number " +
                    "FROM items " +
                    "ORDER BY items.price DESC NULLS FIRST, items.number DESC"
        )
    }

    val pug = Product(number = 88888888, name = "Pug", description = "A funny little dog")

    @Test
    fun `grouping records`() {
        persisted(frenchie).let { id ->
            persisted(Item(productId = id, number = "0001", price = BigDecimal(3199)))
            persisted(Item(productId = id, number = "0002", price = BigDecimal(3299)))
            persisted(Item(productId = id, number = "0003", price = BigDecimal(3399)))
        }

        persisted(pug).let { id ->
            persisted(Item(productId = id, number = "0004", price = BigDecimal(1199)))
            persisted(Item(productId = id, number = "0005", price = BigDecimal(1299)))
        }

        val itemCount = intLiteral("count(items.id)")

        val inventory = Products
            .join(Items, productId, id)
            .slice(name, itemCount)
            .selectAll()
            .groupBy(id)
            .list(recorder) { it[name] to it[itemCount] }

        assertThat(
            "inventory", inventory,hasElement("French Bulldog" to 3) and hasElement("Pug" to 2)
        )

        recorder.assertSql(
            "SELECT products.name, count(items.id) " +
                    "FROM products " +
                    "INNER JOIN items ON items.product_id = products.id " +
                    "GROUP BY products.id"
        )
    }

    @Test
    fun `using a search condition for an aggregate`() {
        persisted(frenchie).let { id ->
            persisted(Item(productId = id, number = "0001", price = BigDecimal(3199)))
            persisted(Item(productId = id, number = "0002", price = BigDecimal(3299)))
            persisted(Item(productId = id, number = "0003", price = BigDecimal(3399)))
        }

        persisted(pug).let { id ->
            persisted(Item(productId = id, number = "0004", price = BigDecimal(1199)))
            persisted(Item(productId = id, number = "0005", price = BigDecimal(1299)))
        }

        val itemCount = intLiteral("count(items.id)")

        val inventory = Products
            .join(Items, productId, id)
            .slice(name, itemCount)
            .selectAll()
            .groupBy(id)
            .having("count(items.id) > 2".asExpression())
            .list(recorder) { it[name] to it[itemCount] }

        assertThat(
            "inventory", inventory,hasElement("French Bulldog" to 3) and !hasElement("Pug" to 2)
        )

        recorder.assertSql(
            "SELECT products.name, count(items.id) " +
                    "FROM products " +
                    "INNER JOIN items ON items.product_id = products.id " +
                    "GROUP BY products.id " +
                    "HAVING count(items.id) > 2"
        )
    }

    @Test
    fun `selecting from a sub query`() {
        persisted(frenchie)
        persisted(pug)

        val dogs = Products.selectAll().alias("dogs")

        val results =
            Select
                .from(dogs)
                .list(recorder) {
                    Product(
                        number = it[dogs[number]],
                        name = it[dogs[name]],
                        description = it[dogs[description]]
                    )
                }

        assertThat(results, equalTo(listOf(frenchie, pug)))

        recorder.assertSql(
            "SELECT dogs.id, dogs.number, dogs.name, dogs.description " +
                "FROM (SELECT products.id, products.number, products.name, products.description FROM products) AS dogs"
        )
    }

    @Test
    fun `joining with a sub query`() {
        persisted(Item(productId = persisted(boxer), number = "543261", price = BigDecimal("1199.00")))
        persisted(Item(productId = persisted(lab), number = "917541", price = BigDecimal("799.00")))

        val dogs = Items.slice(Items.productId, Items.price).selectAll().alias("dogs")

        val records =
            Products
                .join(dogs, condition = "products.id = dogs.product_id")
                .slice(Products)
                .selectWhere("dogs.price > ?", BigDecimal("1000"))
                .list(recorder) { it.product }

        assertThat(records, anyElement(hasName("Boxer")))
        assertThat(records, hasSize(equalTo(1)))

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description " +
                    "FROM products INNER JOIN (SELECT items.product_id, items.price FROM items) AS dogs ON products.id = dogs.product_id " +
                    "WHERE dogs.price > 1000"
        )
    }
}
