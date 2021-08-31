package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.vtence.kabinet.Products.name
import com.vtence.kabinet.Products.number
import kotlin.test.Test

class DeleteStatementTest {
    @Test
    fun `delete from target table`() {
        val delete = DeleteStatement(Products)

        assertThat("sql", delete.asSql(), equalTo("DELETE FROM products"))
    }
}
