package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlin.test.Test

class UpdateStatementTest {
    @Test
    fun `updates specified columns in target table`() {
        val update = UpdateStatement("table", listOf("a", "b", "c"))
        assertThat("sql", update.toSql(), equalTo("UPDATE table SET a = ?, b = ?, c = ?"))
    }
}
