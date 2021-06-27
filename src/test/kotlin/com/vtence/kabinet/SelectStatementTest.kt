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
}
