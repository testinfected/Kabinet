package com.vtence.kabinet

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import java.math.BigDecimal
import java.time.Instant


data class Order(
    var id: Int? = null,
    val number: Long,
    val placedAt: Instant = Instant.now(),
    val lines: List<LineItem> = listOf(),
) {
    operator fun plus(item: Item): Order = copy(lines = lines + item.lineFor(this))

    operator fun plus(items: Iterable<Item>): Order = items.fold(this) { order, item -> order + item }

    val total: BigDecimal get() = lines.fold(BigDecimal.ZERO) { total, item -> total + item.total }
}


data class LineItem(
    var id: Int? = null,
    var orderId: Int?,
    val itemNumber: String,
    val itemUnitPrice: BigDecimal,
    val index: Int
) {
    val quantity = 1

    val total: BigDecimal get() = itemUnitPrice * quantity.toBigDecimal()

    override fun toString(): String {
        return "$itemNumber ($itemUnitPrice$ x $quantity)"
    }
}


fun Item.lineFor(order: Order): LineItem {
    return LineItem(orderId = order.id, itemNumber = number, itemUnitPrice = price!!, index = order.lines.size)
}


object Orders : Table("orders") {
    val id = int("id").autoGenerated()
    val number = long("number")
    val placedAt = timestamp("placed_at")
}

private fun Orders.dehydrate(st: DataSet, order: Order) {
    st[number] = order.number
    st[placedAt] = order.placedAt
}

private fun Orders.hydrate(row: ResultRow): Order {
    return Order(
        id = row[id],
        number = row[number],
        placedAt = row[placedAt],
    )
}

val Order.record: Orders.(DataSet) -> Unit
    get() = { dehydrate(it, this@record) }

val ResultRow.order: Order
    get() = Orders.hydrate(this)


object OrderThat {
    fun hasNumber(number: Long) = has(Order::number, equalTo(number))

    fun wasPlacedAt(instant: Instant) = has(Order::placedAt, equalTo(instant))
}


object LineItems : Table("line_items") {
    val id = int("id").autoGenerated()
    val itemNumber = string("item_number")
    val itemPrice = decimal("item_unit_price", 10, 2)
    val quantity = int("quantity")
    val order = int("order_id")
    val index = int("order_line")
}


private fun LineItems.dehydrate(st: DataSet, line: LineItem) {
    st[itemNumber] = line.itemNumber
    st[itemPrice] = line.itemUnitPrice
    st[quantity] = line.quantity
    st[order] = checkNotNull(line.orderId)
    st[index] = line.index

}

private fun LineItems.hydrate(rs: ResultRow): LineItem {
    return LineItem(
        id = rs[id],
        itemNumber = rs[itemNumber],
        itemUnitPrice = rs[itemPrice],
        orderId = rs[order],
        index = rs[index]
    )
}

val LineItem.record: Dehydrator<LineItems>
    get() = { dehydrate(it, this@record) }

val ResultRow.lineItem: LineItem
    get() = LineItems.hydrate(this)




