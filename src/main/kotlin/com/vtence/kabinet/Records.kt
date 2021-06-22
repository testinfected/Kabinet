package com.vtence.kabinet

import java.sql.ResultSet


typealias KeyHandler<T> = (ResultSet) -> T

val id: KeyHandler<Int> = { it.getInt(1) }

typealias Hydrator<T> = (ResultSet) -> T

