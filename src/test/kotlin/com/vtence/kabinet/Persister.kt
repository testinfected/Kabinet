package com.vtence.kabinet

import java.sql.Connection

class Persister(private val connection: Connection) {

    val transaction = JdbcTransactor(connection)

    operator fun invoke(product: Product): Int {
        return transaction {
            Products.insert(product.record).execute(connection) get Products.id
        }
    }

    operator fun invoke(item: Item): Int {
        return transaction {
            Items.insert(item.record).execute(connection) get Items.id
        }
    }

    operator fun invoke(order: Order): Int {
        return transaction {
            val id = Orders.insert(order.record).execute(connection) get Orders.id
            order.lines.forEach {  line ->
                line.orderId = id
                LineItems.insert(line.record).execute(connection)
            }
            id
        }
    }

    operator fun invoke(payment: Payment): Int {
        return transaction {
            Payments.insert(payment.record).execute(connection) get Payments.id
        }
    }
}