package com.vtence.kabinet

import java.math.BigDecimal
import java.time.LocalDate


data class Payment(
    val id: Int? = null,
    val orderId: Int,
    val amount: BigDecimal,
    val date: LocalDate
)

fun Order.paymentOn(date: LocalDate): Payment {
    return Payment(orderId = checkNotNull(id), amount = total, date = date)
}


object Payments : Table("payments") {
    val id = int("id").autoGenerated()
    val orderId = int("order_id")
    val amount = decimal("amount", 12, 2)
    val date = date("date")
}

private fun dehydrate(payment: Payment): Payments.(Dataset) -> Unit = {
    it[orderId] = payment.orderId
    it[amount] = payment.amount
    it[date] = payment.date
}

fun Payments.hydrate(row: ResultRow): Payment {
    return Payment(
        id = row[id],
        orderId = row[orderId],
        amount = row[amount],
        date = row[date],
    )
}

val Payment.record: Dehydrator<Payments>
    get() = dehydrate(this)

val ResultRow.payment: Payment
    get() = Payments.hydrate(this)