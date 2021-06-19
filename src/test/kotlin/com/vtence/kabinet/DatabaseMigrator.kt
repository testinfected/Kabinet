package com.vtence.kabinet

import org.flywaydb.core.Flyway
import javax.sql.DataSource

class DatabaseMigrator(dataSource: DataSource) {

    private val flyway: Flyway = Flyway.configure()
        .dataSource(dataSource)
        .load()

    fun runMigrations() {
        flyway.migrate()
    }
}
