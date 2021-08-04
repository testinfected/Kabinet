package com.vtence.kabinet

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has


object ProductThat {
    fun hasSameStateAs(other: Product) =
        hasId(other.id) and
        hasNumber(other.number) and
        hasName(other.name) and
        hasDescription(other.description)

    fun hasId(id: Int?) = has(Product::id, equalTo(id))
    fun hasNumber(number: Int) = has(Product::number, equalTo(number))
    fun hasName(name: String) = hasName(equalTo(name))
    fun hasName(matching: Matcher<String>) = has(Product::name, matching)
    fun hasDescription(description: String?) = has(Product::description, equalTo(description))
    fun hasDescription(matching: Matcher<String?>) = has(Product::description, matching)
}
