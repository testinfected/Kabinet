package com.vtence.kabinet

import java.sql.PreparedStatement


fun PreparedStatement.setParameters(parameters: List<*>, offset: Int = 0) {
    for (i in parameters.indices) {
        this[i + offset + 1] = parameters[i]
    }
}


operator fun PreparedStatement.set(index: Int, value: Any?) {
    val sqlType = parameterMetaData.getParameterType(index)
    setObject(index, value, sqlType)
}
