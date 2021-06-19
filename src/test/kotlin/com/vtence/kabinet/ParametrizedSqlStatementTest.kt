package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test

class ParametrizedSqlStatementTest {

    @Test
    fun `replaces parameters by question marks`() {
        val statement = ParametrizedSqlStatement("SELECT * FROM vegetables WHERE color = :color AND taste = :taste")
        statement["color"] = "green"
        statement["taste"] = "sweet"
        assertThat("sql", statement.toSql(), equalTo("SELECT * FROM vegetables WHERE color = ? AND taste = ?"))
    }

    @Test
    fun `correctly handles parameters names containing other parameter names`() {
        val statement = ParametrizedSqlStatement("SELECT * FROM vegetables WHERE color = :color OR color = :color_bis")
        statement["color"] = "green"
        statement["color_bis"] = "red"
        assertThat("sql", statement.toSql(), equalTo("SELECT * FROM vegetables WHERE color = ? OR color = ?"))
    }
}
