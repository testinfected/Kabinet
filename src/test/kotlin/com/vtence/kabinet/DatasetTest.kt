package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.isA
import com.natpryce.hamkrest.throws
import org.junit.jupiter.api.Test

class DatasetTest {

    val data = Dataset(Products)

    @Test
    fun `complains when column already present in set`() {
        data[Products.number] = "77777777"
        data[Products.name] = "French Bulldog"

        assertThat({ data[Products.number] = "66666666" }, throws(isA<Exception>()))
    }
}
