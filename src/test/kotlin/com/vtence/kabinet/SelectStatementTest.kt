package com.vtence.kabinet

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlin.test.Test

class SelectStatementTest {
    @Test
    fun `selects columns in set`() {
        val select = SelectStatement(Products)

        assertThat(
            select.toSql(), equalTo(
                "SELECT products.id, products.number, products.name, products.description FROM products"
            )
        )
    }

    @Test
    fun `supports limits conditions`() {
        val select = SelectStatement(Products).limitTo(1, start = 0)

        assertThat(
            select.toSql(), equalTo(
                "SELECT products.id, products.number, products.name, products.description FROM products LIMIT 1"
            )
        )
    }

    @Test
    fun `supports offsets in limits`() {
        val select = SelectStatement(Products)
            .limitTo(10, start = 100)

        assertThat(
            select.toSql(), equalTo(
                "SELECT products.id, products.number, products.name, products.description FROM products LIMIT 10 OFFSET 100"
            )
        )
    }

    @Test
    fun `supports where conditions`() {
        val select = SelectStatement(Products).where { it.append("number = 100 AND name = 'Lab'") }

        assertThat(
            "sql", select.toSql(), equalTo(
                "SELECT products.id, products.number, products.name, products.description " +
                        "FROM products WHERE number = 100 AND name = 'Lab'"
            )
        )
    }

    @Test
    fun `supports arguments in where conditions`() {
        val select = SelectStatement(Products).where {
            it.append("number = ")
            it.appendArgument(IntColumnType, 10001000)
            it.append(" AND ")
            it.append("name = ")
            it.appendArgument(StringColumnType, "Chihuahua")
        }

        assertThat(
            "sql", select.toSql(), equalTo(
                "SELECT products.id, products.number, products.name, products.description " +
                        "FROM products WHERE number = 10001000 AND name = 'Chihuahua'"
            )
        )
        assertThat(
            "prepared sql", select.toSql(prepared = true), equalTo(
                "SELECT products.id, products.number, products.name, products.description " +
                        "FROM products WHERE number = ? AND name = ?"
            )
        )
        assertThat(
            "args", select.arguments(), equalTo(
                listOf(
                    IntColumnType to 10001000,
                    StringColumnType to "Chihuahua"
                )
            )
        )
    }

    @Test
    fun `toggles to counting instead of selecting`() {
        val select = SelectStatement(Products).countOnly()

        assertThat("sql", select.toSql(), equalTo("SELECT COUNT(*) FROM products"))
    }

    @Test
    fun `supports distinct clause`() {
        val select = SelectStatement(Products).distinctOnly()
        assertThat("sql", select.toSql(), equalTo("SELECT DISTINCT products.id, products.number, products.name, products.description FROM products"))
    }

    @Test
    fun `combines distinct and count`() {
        val select = SelectStatement(Products)
            .distinctOnly()
            .countOnly()
        assertThat("sql", select.toSql(), equalTo(
            "SELECT COUNT(DISTINCT (products.id, products.number, products.name, products.description)) FROM products"
        ))
    }

    @Test
    fun `supports group by clauses`() {
        val select = SelectStatement(Products.slice(Products.name, Products.description, intLiteral("count(products.id)")))
            .groupBy("name".asExpression<Nothing>(), "description".asExpression<Nothing>())

        assertThat("sql", select.toSql(), equalTo(
            "SELECT products.name, products.description, count(products.id) FROM products GROUP BY name, description"
        ))
    }

    @Test
    fun `supports having clauses`() {
        val select = SelectStatement(Products.slice(Products.name, intLiteral("count(products.id)")))
            .groupBy("name".asExpression<Nothing>())
            .having("count(products.id) > ?".asExpression(2))

        assertThat("sql", select.toSql(), equalTo(
            "SELECT products.name, count(products.id) FROM products GROUP BY name HAVING count(products.id) > 2"
        ))
    }

    @Test
    fun `drops group by clause when counting`() {
        val select = SelectStatement(Products).groupBy(Products.number).countOnly()

        assertThat("sql", select.toSql(), equalTo(
            "SELECT COUNT(*) FROM products"
        ))
    }

    @Test
    fun `supports order by clauses`() {
        val select = SelectStatement(Products.slice(Products.id))
            .orderBy("name".asExpression<Nothing>(), "number DESC NULLS FIRST".asExpression<Nothing>())

        assertThat("sql", select.toSql(), equalTo(
            "SELECT products.id FROM products ORDER BY name, number DESC NULLS FIRST"
        ))
    }

    @Test
    fun `drops order by clause when counting`() {
        val select = SelectStatement(Products).orderBy(Products.number).countOnly()

        assertThat("sql", select.toSql(), equalTo(
            "SELECT COUNT(*) FROM products"
        ))
    }

    @Test
    fun `outputs clauses in proper order`() {
        val select = SelectStatement(Products.slice(Products.name, intLiteral("count(products.id)")))
            .orderBy(Products.name)
            .having("count(products.id) > 2".asExpression())
            .groupBy(Products.name)
            .where("name <> ''".asExpression())

        assertThat("sql", select.toSql(), equalTo(
            "SELECT products.name, count(products.id) FROM products WHERE name <> '' GROUP BY products.name HAVING count(products.id) > 2 ORDER BY products.name"
        ))
    }
}
