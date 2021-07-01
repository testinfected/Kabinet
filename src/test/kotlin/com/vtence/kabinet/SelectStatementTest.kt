package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlin.test.Test

class SelectStatementTest() {
    @Test
    fun `selects specified columns from target table`() {
        val select = SelectStatement("table", listOf("a", "b", "table.c"))
        assertThat(select.toSql(), equalTo("SELECT a, b, table.c FROM table"))
    }

    @Test
    fun `supports limits conditions`() {
        val select = SelectStatement("table", listOf("a", "b", "c"))
        select.limitTo(1, start = 0)
        assertThat("sql", select.toSql(), equalTo("SELECT a, b, c FROM table LIMIT 1"))
    }

    @Test
    fun `supports offsets in limits`() {
        val select = SelectStatement("table", listOf("a", "b", "c"))
        select.limitTo(10, start = 100)
        assertThat("sql", select.toSql(), equalTo("SELECT a, b, c FROM table LIMIT 10 OFFSET 100"))
    }
}
