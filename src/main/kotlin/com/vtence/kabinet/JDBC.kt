package com.vtence.kabinet

import java.sql.PreparedStatement


fun PreparedStatement.setParameters(parameters: List<Argument<*>>) {
    for (i in parameters.indices) {
        this[i + 1] = parameters[i]
    }
}

operator fun PreparedStatement.set(index: Int, argument: Argument<*>) {
    val (type, value) = argument
    type.set(this, index, value)
}
