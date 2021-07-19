package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.vtence.kabinet.Products.description
import com.vtence.kabinet.Products.name
import kotlin.test.Test

class UpdateStatementTest {
    @Test
    fun `updates specified columns in target table`() {
        val update = UpdateStatement(Products.slice(name, description))

        assertThat("sql", update.toSql(), equalTo(
            "UPDATE products SET name = ?, description = ?"
        ))
    }
}
