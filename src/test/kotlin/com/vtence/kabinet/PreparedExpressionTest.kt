package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isEmpty
import kotlin.test.Test

class PreparedExpressionTest {

    @Test
    fun `preserves parameterless expression`() {
        val expression = PreparedExpression("name = 'Bulldog'", listOf())

        assertThat("sql", expression.asSql(), equalTo("name = 'Bulldog'"))
        assertThat("args", expression.arguments(), isEmpty)
    }

    @Test
    fun `replaces question marks with parameters, in order`() {
        val expression = PreparedExpression("number = ? AND name = ?", listOf(12345678, "Mastiff"))

        assertThat("raw sql", expression.asSql(), equalTo(
            "number = 12345678 AND name = 'Mastiff'"
        ))
        assertThat("prepared sql", expression.asSql(prepared = true), equalTo(
            "number = ? AND name = ?"
        ))
        assertThat("args", expression.arguments(), equalTo(
            listOf(IntColumnType to 12345678, StringColumnType to "Mastiff")
        ))
    }

    @Test
    fun `ignores doubled question marks`() {
        val expression = PreparedExpression("number = ? AND thing ?? ...", listOf(12345678))

        assertThat("raw sql", expression.asSql(), equalTo(
            "number = 12345678 AND thing ?? ..."
        ))
        assertThat("prepared sql", expression.asSql(prepared = true), equalTo(
            "number = ? AND thing ?? ..."
        ))
        assertThat("args", expression.arguments(), equalTo(
            listOf(IntColumnType to 12345678)
        ))
    }

    @Test
    fun `ignores quoted question marks`() {
        val expression = PreparedExpression("foo = \"?\" AND bar = '?' and baz = '\"?'", listOf())

        assertThat("raw sql", expression.asSql(), equalTo(
            "foo = \"?\" AND bar = '?' and baz = '\"?'"
        ))
        assertThat("prepared sql", expression.asSql(prepared = true), equalTo(
            "foo = \"?\" AND bar = '?' and baz = '\"?'"
        ))
        assertThat("args", expression.arguments(), isEmpty)
    }
}
