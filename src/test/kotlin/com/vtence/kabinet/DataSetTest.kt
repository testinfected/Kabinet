package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isEmpty
import com.natpryce.hamkrest.throws
import com.vtence.kabinet.Products.description
import com.vtence.kabinet.Products.id
import com.vtence.kabinet.Products.name
import com.vtence.kabinet.Products.number
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class DataSetTest {

    @Nested
    inner class Closed {
        val closed = DataSet.closed(Products.columns)

        @Test
        fun `reports base columns in order`() {
            assertThat(closed.columns, equalTo(Products.columns))
        }

        @Test
        fun `reports parameters in columns order`() {
            closed[name] = "Boston"
            closed[description] = "A small terrier dog"
            closed[number] = 66666666
            closed[id] = 1

            assertThat(closed.values, equalTo(listOf(
                1, 66666666, "Boston", "A small terrier dog"
            )))
        }

        @Test
        fun `assumes null when column not set`() {
            closed[name] = "Boston"
            closed[number] = 66666666

            assertThat(closed.values, equalTo(listOf(
                null, 66666666, "Boston", null
            )))
        }

        @Test
        fun `complains when column already present in set`() {
            closed[number] = 77777777
            closed[name] = "French Bulldog"

            assertThat({ closed[number] = 66666666 }, throws<IllegalStateException>())
        }
    }

    @Nested
    inner class Opened {
        val opened = DataSet.opened()

        @Test
        fun `reports parameters in set order`() {
            opened[name] = "Boston"
            opened[number] = 66666666

            assertThat(opened.values, equalTo(listOf(
                "Boston", 66666666
            )))
        }

        @Test
        fun `is without a base, reporting columns in the order they are set`() {
            assertThat("initially", opened.columns, isEmpty)

            opened[name] = "French Bulldog"
            opened[description] = "A friendly dog"

            assertThat("set columns", opened.columns, equalTo(listOf(name, description)))
        }
    }
}
