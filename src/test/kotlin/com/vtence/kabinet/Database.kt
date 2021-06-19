package com.vtence.kabinet

import java.sql.Connection
import javax.sql.DataSource

class Database(private val dataSource: DataSource) {

    fun connect(): Connection = dataSource.connection

    fun migrate() {
        DatabaseMigrator(dataSource).runMigrations()
    }

    companion object {
        fun inMemory(): Database = connect(url = "jdbc:h2:mem:test", username = "kabinet", password = "test")

        fun connect(url: String, username: String, password: String?): Database =
            Database(AutoSelectDataSource.inAutoCommitMode(url, username, password))
    }
}
