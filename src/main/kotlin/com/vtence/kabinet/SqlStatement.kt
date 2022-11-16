package com.vtence.kabinet

fun interface SqlStatement {
    fun build(statement: SqlBuilder)
}

fun SqlStatement.build(prepared: Boolean = false) = buildStatement(prepared) { this@build.build(this) }

fun SqlStatement.toSql(prepared: Boolean = false) = build(prepared).asSql()

fun SqlStatement.arguments() = build(false).arguments
