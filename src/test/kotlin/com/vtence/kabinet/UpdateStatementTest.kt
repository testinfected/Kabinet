package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.vtence.kabinet.Products.description
import com.vtence.kabinet.Products.name
import com.vtence.kabinet.Products.number
import kotlin.test.Test

class UpdateStatementTest {
    @Test
    fun `updates specified columns in target table`() {
        val update = UpdateStatement(Products.slice(number, name), listOf(77777777, "Frenchie"))

        assertThat("raw sql", update.asSql(), equalTo(
            "UPDATE products SET number = 77777777, name = 'Frenchie'"
        ))
        assertThat("prepared sql", update.asSql(prepared = true), equalTo(
            "UPDATE products SET number = ?, name = ?"
        ))
        assertThat("parameters", update.arguments(), equalTo(
            listOf(IntColumnType to 77777777, StringColumnType to "Frenchie")
        ))
    }

    @Test
    fun `supports where conditions with arguments`() {
        val update = UpdateStatement(Products.slice(name), listOf("Frenchie")).where {
            it.append("number = ")
            it.appendArgument(IntColumnType to 77777777)
        }

        assertThat("raw sql", update.asSql(), equalTo(
            "UPDATE products SET name = 'Frenchie' WHERE number = 77777777"
        ))
        assertThat("prepared sql", update.asSql(prepared = true), equalTo(
            "UPDATE products SET name = ? WHERE number = ?"
        ))
        assertThat("parameters", update.arguments(), equalTo(
            listOf(StringColumnType to "Frenchie", IntColumnType to 77777777)
        ))
    }
}
