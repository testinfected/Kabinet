package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.equalToIgnoringCase
import com.natpryce.hamkrest.sameInstance
import kotlin.test.AfterTest
import kotlin.test.Test

class AutoSelectDataSourceTest {
    val source = AutoSelectDataSource("jdbc:h2:mem:test", "test")

    val connection = source.connection

    @AfterTest
    fun closeConnection() {
        connection.close()
    }

    @Test
    fun `configures connection`() {
        val metaData = connection.metaData
        assertThat("url", metaData.url, equalTo("jdbc:h2:mem:test"))
        assertThat("username", metaData.userName, equalToIgnoringCase("test"))
    }

    @Test
    fun `uses fresh connections`() {
        source.connection.use { other ->
            assertThat(
                "other connection",
                other,
                !sameInstance(connection)
            )
        }
    }

    @Test
    fun `auto commits by default`() {
        assertThat("auto commit", connection.autoCommit, equalTo(true))
    }

    @Test
    fun `sets auto commit`() {
        source.toggleAutoCommit(false)
        source.connection.use { connection ->
            assertThat(
                "auto commit",
                connection.autoCommit,
                equalTo(false)
            )
        }
    }
}
