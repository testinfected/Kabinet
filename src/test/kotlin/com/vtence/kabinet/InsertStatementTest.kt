package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlin.test.Test

class InsertStatementTest {
    @Test
    fun `inserts specified columns in target table`() {
        val insert = InsertStatement("table", listOf("a", "b", "c"))
        assertThat("sql", insert.toSql(), equalTo("INSERT INTO table(a, b, c) VALUES(?, ?, ?)"))
    }
}
