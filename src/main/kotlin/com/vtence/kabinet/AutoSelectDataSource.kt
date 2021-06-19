package com.vtence.kabinet

import java.io.OutputStream
import java.io.PrintWriter
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLFeatureNotSupportedException
import java.util.logging.Logger
import javax.sql.DataSource


abstract class AbstractDataSource : DataSource {
    override fun getLogWriter(): PrintWriter {
        return PrintWriter(object : OutputStream() {
            override fun write(b: Int) {}
        })
    }

    override fun setLogWriter(out: PrintWriter) {}
    override fun setLoginTimeout(seconds: Int) {}
    override fun getLoginTimeout(): Int = 0

    override fun <T> unwrap(type: Class<T>): T {
        require(isDataSource(type)) { type.name }
        return type.cast(this)
    }

    override fun isWrapperFor(type: Class<*>): Boolean = isDataSource(type)
    private fun isDataSource(type: Class<*>): Boolean = DataSource::class.java == type

    override fun getParentLogger(): Logger =
        throw SQLFeatureNotSupportedException("This data source does not use logging")
}


class AutoSelectDataSource(
    private val url: String,
    private val username: String,
    private val password: String? = null,
    private var autoCommit: Boolean = true
) : AbstractDataSource() {

    override fun getConnection(): Connection = getConnection(username, password)

    override fun getConnection(username: String?, password: String?): Connection {
        val connection = DriverManager.getConnection(url, username, password)
        connection.autoCommit = autoCommit
        return connection
    }

    fun toggleAutoCommit(on: Boolean): AutoSelectDataSource = apply { autoCommit = on }

    companion object {
        fun inAutoCommitMode(url: String, username: String, password: String? = null) =
            AutoSelectDataSource(url, username, password, true)

        fun inTransactionalMode(url: String, username: String, password: String? = null) =
            AutoSelectDataSource(url, username, password, false)
    }
}
