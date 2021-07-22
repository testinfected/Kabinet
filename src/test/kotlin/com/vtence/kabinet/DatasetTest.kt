package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isA
import com.natpryce.hamkrest.isEmpty
import com.natpryce.hamkrest.throws
import com.vtence.kabinet.Products.description
import com.vtence.kabinet.Products.name
import com.vtence.kabinet.Products.number
import org.junit.jupiter.api.Test

class DatasetTest {

    val closedSet = Dataset.closed(Products.columns)
    val openSet = Dataset.opened()

    @Test
    fun `complains when column already present in set`() {
        closedSet[number] = "77777777"
        closedSet[name] = "French Bulldog"

        assertThat({ closedSet[number] = "66666666" }, throws<IllegalStateException>())
    }

    @Test
    fun `is closed with a base, reporting base columns`() {
        assertThat(closedSet.columns, equalTo(Products.columns))
    }

    @Test
    fun `is opened without a base, reporting columns in set order`() {
        assertThat("initially", openSet.columns, isEmpty)

        openSet[name] = "French Bulldog"
        openSet[description] = "A friendly dog"

        assertThat("set columns", openSet.columns, equalTo(listOf(name, description)))
    }
}
