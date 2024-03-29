package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test

class ParametrizedStatementTest {

    @Test
    fun `replaces parameters by their values`() {
        val statement = ParameterizedStatement(
            "SELECT * FROM vegetables WHERE color = :color AND taste = :taste AND length = :length"
        )
        statement["taste"] = "sweet"
        statement["length"] = 10
        statement["color"] = "green"

        assertThat(
            "raw sql", statement.toSql(),
            equalTo("SELECT * FROM vegetables WHERE color = 'green' AND taste = 'sweet' AND length = 10")
        )

        assertThat(
            "prepared sql", statement.toSql(prepared = true),
            equalTo("SELECT * FROM vegetables WHERE color = ? AND taste = ? AND length = ?")
        )

        assertThat(
            "args", statement.arguments(),
            equalTo(
                listOf(
                    AutoDetectColumnType to "green",
                    AutoDetectColumnType to "sweet",
                    IntColumnType to 10
                )
            )
        )
    }

    @Test
    fun `correctly handles confusing parameter names`() {
        val statement = ParameterizedStatement(
            "SELECT * FROM vegetables WHERE color_gradient = :color_gradient AND color = :color"
        )
        statement["color"] = "green"
        statement["color_gradient"] = "linear"

        assertThat(
            "sql", statement.toSql(),
            equalTo("SELECT * FROM vegetables WHERE color_gradient = 'linear' AND color = 'green'")
        )
    }

    @Test
    fun `only replaces known parameter names`() {
        val statement = ParameterizedStatement(
            "SELECT * FROM vegetables WHERE color_gradient = :color_gradient AND color = 'green' ::COLOR"
        )
        statement["color_gradient"] = "linear"

        assertThat(
            "sql", statement.toSql(),
            equalTo("SELECT * FROM vegetables WHERE color_gradient = 'linear' AND color = 'green' ::COLOR")
        )
    }

    @Test
    fun `correctly figures out parameters positions in statement`() {
        val statement = ParameterizedStatement(
            "INSERT INTO table VALUES(:ffffff, :bb, :hhhhhhhh, :eeeee, :ccc, :dddd, :ggggggg, :a)"
        )

        statement["a"] = "a"
        statement["bb"] = "b"
        statement["ccc"] = "c"
        statement["dddd"] = "d"
        statement["eeeee"] = "e"
        statement["ffffff"] = "f"
        statement["ggggggg"] = "g"
        statement["hhhhhhhh"] = "h"

        assertThat(
            "sql", statement.toSql(),
            equalTo("INSERT INTO table VALUES('f', 'b', 'h', 'e', 'c', 'd', 'g', 'a')")
        )
    }
}
