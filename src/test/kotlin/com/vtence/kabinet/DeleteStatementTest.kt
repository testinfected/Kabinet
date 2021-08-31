package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlin.test.Test

class DeleteStatementTest {
    @Test
    fun `delete from target table`() {
        val delete = DeleteStatement(Products)

        assertThat("raw sql", delete.toSql(), equalTo("DELETE FROM products"))
    }

    @Test
    fun `supports where conditions`() {
        val delete = DeleteStatement(Products).where {
                it.append("number = ")
                it.appendArgument(IntColumnType to 77777777)
            }

        assertThat("raw sql", delete.toSql(), equalTo("DELETE FROM products WHERE number = 77777777"))

        assertThat("prepared sql", delete.toSql(prepared = true), equalTo(
            "DELETE FROM products WHERE number = ?"
        ))
        assertThat("parameters", delete.arguments(), equalTo(
            listOf(IntColumnType to 77777777)
        ))
    }
}
