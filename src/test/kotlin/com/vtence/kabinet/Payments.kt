package com.vtence.kabinet

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


data class Payment(
    val id: Int? = null,
    val orderId: Int,
    val amount: BigDecimal,
    val date: LocalDate,
    val time: LocalTime
)

fun Order.paymentAt(moment: LocalDateTime): Payment {
    return Payment(orderId = checkNotNull(id), amount = total, date = moment.toLocalDate(), time = moment.toLocalTime())
}


object Payments : Table("payments") {
    val id = int("id").autoGenerated()
    val orderId = int("order_id")
    val amount = decimal("amount", 12, 2)
    val date = date("date")
    val time = time("time")
}

private fun dehydrate(payment: Payment): Payments.(DataSet) -> Unit = {
    it[orderId] = payment.orderId
    it[amount] = payment.amount
    it[date] = payment.date
    it[time] = payment.time
}

fun Payments.hydrate(row: ResultRow): Payment {
    return Payment(
        id = row[id],
        orderId = row[orderId],
        amount = row[amount],
        date = row[date],
        time = row[time]
    )
}

val Payment.record: Dehydrator<Payments>
    get() = dehydrate(this)

val ResultRow.payment: Payment
    get() = Payments.hydrate(this)


object PaymentThat {
    fun hasDate(on: LocalDate) = has(Payment::date, equalTo(on))
}