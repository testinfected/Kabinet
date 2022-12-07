package com.vtence.kabinet


fun Expression<*>.toSql() = SqlStatementBuilder(prepared = false).also { it.append(this) }.asSql()