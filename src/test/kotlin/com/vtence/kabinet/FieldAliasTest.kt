package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test

class FieldAliasTest {

    @Test
    fun `builds aliased expression`() {
        val alias = Products.name.alias("product_name")

        assertThat(alias.toSql(), equalTo("""products.name AS product_name"""))
    }

    @Test
    fun `provides alias only expression form`() {
        val alias = Products.name.alias("product_name")

        assertThat(alias.aliasOnly().toSql(), equalTo("""product_name"""))
    }
}
