package com.vtence.kabinet

import java.math.BigDecimal

data class Item(
    val id: Int? = null,
    val productId: Int,
    val number: String,
    val price: BigDecimal? = null
)

object Items : Table("items") {
    val id = int("id").autoGenerated()
    val number = string("number")
    val productId = int("product_id")
    val price = decimal("price", 12, 2).nullable()
}


private fun Items.dehydrate(st: Dataset, item: Item) {
    st[productId] = item.productId
    st[number] = item.number
    st[price] = item.price
}

private fun Items.hydrate(row: ResultRow): Item {
    return Item(
        id = row[id],
        productId = row[productId],
        number = row[number],
        price = row[price]
    )
}

val Item.record: Items.(Dataset) -> Unit
    get() = { dehydrate(it, this@record) }

val ResultRow.item: Item
    get() = Items.hydrate(this)

fun ResultRow.item(alias: String): Item = rebase(Items.alias(alias)).item