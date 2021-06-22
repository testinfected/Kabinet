package com.vtence.kabinet

import Product
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has


object ProductThat {
    fun hasSameStateAs(other: Product) =
        hasId(other.id) and
        hasNumber(other.number) and
        hasName(other.name) and
        hasDescription("A muscular, heavy dog")

    fun hasId(id: Int?) = has(Product::id, equalTo(id))
    fun hasNumber(number: String) = has(Product::number, equalTo(number))
    fun hasName(name: String) = has(Product::name, equalTo(name))
    fun hasDescription(description: String) = has(Product::description, equalTo(description))
}
