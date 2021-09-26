package com.vtence.kabinet

import com.natpryce.hamkrest.anyElement
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.hasElement
import com.vtence.kabinet.ItemThat.hasNumber
import com.vtence.kabinet.ProductThat.hasName
import java.math.BigDecimal
import java.time.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ExpressionParametersTest {

    val database = Database.inMemory()
    val connection = database.openConnection()

    val persist = Persister(connection)
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
    fun `using plain strings in expressions`() {
        persist(Product(number = 666666, name = "Great Dane"))
        persist(Product(number = 888888, name = "Boxer"))

        val selection =
            Products
                .selectWhere("name like ?", "Great%")
                .list(recorder) { it.product }

        assertThat(
            "selection", selection, anyElement(hasName("Great Dane"))
        )

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description " +
                    "FROM products " +
                    "WHERE name like 'Great%'"
        )
    }

    @Test
    fun `using numbers in expressions`() {
        persist(Product(number = 666666, name = "Great Dane"))
        persist(Product(number = 888888, name = "Boxer"))

        val selection =
            Products
                .selectWhere("number > ?", 777777)
                .list(recorder) { it.product }

        assertThat(
            "selection", selection, anyElement(hasName("Boxer"))
        )

        recorder.assertSql(
            "SELECT products.id, products.number, products.name, products.description " +
                    "FROM products " +
                    "WHERE number > 777777"
        )
    }

    @Test
    fun `using boolean values in expressions`() {
        val frenchie = persist(Product(number = 777777, name = "Frenchie"))

        persist(Item(productId = frenchie, number = "543261", price = BigDecimal("4199.00")))
        persist(Item(productId = frenchie, number = "917541", price = BigDecimal("3799.00"), onSale = true))

        val selection =
            Items
                .selectWhere("on_sale = ?", true)
                .list(recorder) { it.item }

        assertThat(
            "selection", selection, anyElement(hasNumber("917541"))
        )

        recorder.assertSql(
            "SELECT items.id, items.number, items.product_id, items.price, items.on_sale " +
                    "FROM items " +
                    "WHERE on_sale = TRUE"
        )
    }

    @Test
    fun `using decimal values in expressions`() {
        val frenchie = persist(Product(number = 777777, name = "Frenchie"))

        persist(Item(productId = frenchie, number = "543261", price = BigDecimal("4199.00")))
        persist(Item(productId = frenchie, number = "917541", price = BigDecimal("3799.00"), onSale = true))

        val selection =
            Items
                .selectWhere("price <= ?", BigDecimal(4000).setScale(2))
                .list(recorder) { it.item }

        assertThat(
            "selection", selection, anyElement(hasNumber("917541"))
        )

        recorder.assertSql(
            "SELECT items.id, items.number, items.product_id, items.price, items.on_sale " +
                    "FROM items " +
                    "WHERE price <= 4000.00"
        )
    }

    @Test
    fun `using instants in expressions`() {
        fun midnightOn(date: LocalDate)  = ZonedDateTime.of(date, LocalTime.MIDNIGHT, ZoneId.systemDefault()).toInstant()

        persist(Order(number = "10000000", placedAt = midnightOn(LocalDate.parse("2021-08-05"))))
        persist(Order(number = "10000001", placedAt = midnightOn(LocalDate.parse("2021-09-20"))))

        val selection =
            Orders
                .selectWhere("placed_at > ?", midnightOn(LocalDate.parse("2021-09-01")))
                .list(recorder) { it.order }

        assertThat(
            "selection", selection, anyElement(OrderThat.hasNumber("10000001"))
        )

        recorder.assertSql(
            "SELECT orders.id, orders.number, orders.placed_at FROM orders WHERE placed_at > '2021-09-01 00:00:00.0'"
        )
    }

    @Test
    fun `using local dates in expressions`() {
        val paid = Order(number = "10000000").also {
            it.id = persist(it)
        }

        persist(paid.paymentOn(LocalDate.parse("2021-10-05")))
        persist(paid.paymentOn(LocalDate.parse("2020-08-05")))

        val dates =
            Payments
                .slice(Payments.date)
                .selectWhere("date > ?", LocalDate.parse("2021-01-01"))
                .list(recorder) { it[Payments.date] }

        assertThat(
            "dates", dates, hasElement(LocalDate.parse("2021-10-05"))
        )

        recorder.assertSql(
            "SELECT payments.date FROM payments WHERE date > '2021-01-01'"
        )
    }
}

