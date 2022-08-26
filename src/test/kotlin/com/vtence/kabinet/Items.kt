package com.vtence.kabinet

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import java.math.BigDecimal

data class Item(
    var id: Int? = null,
    val productId: Int,
    val number: String,
    val price: BigDecimal? = null,
    val onSale: Boolean = false
)

object Items : Table("items") {
    val id = int("id").autoGenerated()
    val number = string("number")
    val productId = int("product_id")
    val price = decimal("price", 12, 2).nullable()
    val onSale = boolean("on_sale")
}


private fun Items.dehydrate(st: DataSet, item: Item) {
    st[productId] = item.productId
    st[number] = item.number
    st[price] = item.price
    st[onSale] = item.onSale
}

private fun Items.hydrate(rs: ResultRow): Item {
    return Item(
        id = rs[id],
        productId = rs[productId],
        number = rs[number],
        price = rs[price],
        onSale = rs[onSale]
    )
}

val Item.record: Dehydrator<Items>
    get() = { dehydrate(it, this@record) }

val ResultRow.item: Item
    get() = Items.hydrate(this)

fun ResultRow.item(alias: String): Item = rebase(Items.alias(alias)).item


object ItemThat {
    fun hasNumber(number: String) = has(Item::number, equalTo(number))
}