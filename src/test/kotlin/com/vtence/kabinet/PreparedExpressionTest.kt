package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isEmpty
import kotlin.test.Test

class PreparedExpressionTest {

    @Test
    fun `preserves parameterless expression`() {
        val expression = PreparedExpression<Boolean>("name = 'Bulldog'", listOf())

        assertThat("sql", expression.toSql(), equalTo("name = 'Bulldog'"))
        assertThat("args", expression.arguments(), isEmpty)
    }

    @Test
    fun `replaces question marks with parameters, in order`() {
        val expression = PreparedExpression<Boolean>("number = ? AND name = ?", listOf(12345678, "Mastiff"))

        assertThat("raw sql", expression.toSql(), equalTo(
            "number = 12345678 AND name = 'Mastiff'"
        ))
        assertThat("prepared sql", expression.toSql(prepared = true), equalTo(
            "number = ? AND name = ?"
        ))
        assertThat("args", expression.arguments(), equalTo(
            listOf(IntColumnType to 12345678, AutoDetectColumnType to "Mastiff")
        ))
    }

    @Test
    fun `handles null parameter values`() {
        val expression = PreparedExpression<Boolean>("description IS ?", listOf(null))

        assertThat("sql", expression.toSql(), equalTo("description IS NULL"))
        assertThat("args", expression.arguments(), equalTo(listOf(AutoDetectColumnType to null)))
    }

    @Test
    fun `ignores doubled question marks`() {
        val expression = PreparedExpression<Boolean>("number = ? AND thing ?? ...", listOf(12345678))

        assertThat("raw sql", expression.toSql(), equalTo(
            "number = 12345678 AND thing ?? ..."
        ))
        assertThat("prepared sql", expression.toSql(prepared = true), equalTo(
            "number = ? AND thing ?? ..."
        ))
        assertThat("args", expression.arguments(), equalTo(
            listOf(IntColumnType to 12345678)
        ))
    }

    @Test
    fun `ignores quoted question marks`() {
        val expression = PreparedExpression<Boolean>("foo = \"?\" AND bar = '?' and baz = '\"?'", listOf())

        assertThat("raw sql", expression.toSql(), equalTo(
            "foo = \"?\" AND bar = '?' and baz = '\"?'"
        ))
        assertThat("prepared sql", expression.toSql(prepared = true), equalTo(
            "foo = \"?\" AND bar = '?' and baz = '\"?'"
        ))
        assertThat("args", expression.arguments(), isEmpty)
    }
}
