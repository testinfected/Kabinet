package com.vtence.kabinet

import Product
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has


object ProductThat {
    fun hasId(id: Int) = has(Product::id, equalTo(id))
    fun hasNumber(number: String) = has(Product::number, equalTo(number))
    fun hasName(name: String) = has(Product::name, equalTo(name))
    fun hasDescription(description: String) = has(Product::description, equalTo(description))
}
