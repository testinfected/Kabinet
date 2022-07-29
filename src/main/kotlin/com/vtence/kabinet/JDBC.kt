package com.vtence.kabinet

import java.sql.PreparedStatement


fun PreparedStatement.set(arguments: List<Argument<*>>) {
    for (i in arguments.indices) {
        this[i + 1] = arguments[i]
    }
}

operator fun PreparedStatement.set(index: Int, argument: Argument<*>) {
    val (type, value) = argument
    type.set(this, index, value)
}
