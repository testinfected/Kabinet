package com.vtence.kabinet

import java.sql.ResultSet

typealias Hydrator<T> = (ResultSet) -> T

typealias KeyHandler<T> = (ResultSet) -> T

val id: KeyHandler<Int> = { it.getInt(1) }

