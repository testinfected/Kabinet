package com.vtence.kabinet

import java.sql.PreparedStatement
import java.sql.ResultSet


typealias Hydrator<T> = (ResultSet) -> T

typealias Dehydrator = (PreparedStatement) -> Unit

typealias KeyHandler<T> = (ResultSet) -> T

val id: KeyHandler<Int> = { it.getInt(1) }

