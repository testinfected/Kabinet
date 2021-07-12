package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test


class ColumnTest {
    object OtherTable : Table("other") {
        val name = string("name")
    }

    val productName = Column(Products, "name", StringColumnType)
    val productNumber = Column(Products, "number", IntColumnType)

    @Test
    fun `knows its fully qualified name`() {
        assertThat(Products.name.qualifiedName, equalTo("products.name"))
    }

    @Test
    fun `defines equality on table and column names`() {
        assertThat("same instance", Products.name, equalTo(Products.name))
        assertThat("same definition", Products.name, equalTo(productName))
        assertThat("different types", Products.number, equalTo(productNumber))
        assertThat("different tables", Products.name, !equalTo(OtherTable.name))
    }

    @Test
    fun `defines hash code as well on table and column names`() {
        assertThat("same instance", Products.name.hashCode(), equalTo(Products.name.hashCode()))
        assertThat("same definition", Products.name.hashCode(), equalTo(productName.hashCode()))
        assertThat("different types", Products.number.hashCode(), equalTo(productNumber.hashCode()))
        assertThat("different tables", Products.name.hashCode(), !equalTo(OtherTable.name.hashCode()))
    }
}
