package com.vtence.kabinet

import java.sql.Connection

typealias UnitOfWork<T> = () -> T

interface Transactor {
    operator fun <T> invoke(work: UnitOfWork<T>): T
}

class JdbcTransactor(private val connection: Connection) : Transactor {
    override fun <T> invoke(work: UnitOfWork<T>): T {
        try {
            return work().also { connection.commit() }
        } catch (e: Throwable) {
            connection.rollback()
            throw e
        }
    }
}
