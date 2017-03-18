package com.geek.sc.config;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import com.geek.sc.properties.FlywayProperties;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlywayMigrationManager {
    private static final Logger log = LoggerFactory.getLogger(FlywayMigrationManager.class);

    private static Flyway flyway = new Flyway();

    private DataSource dataSource;

    private FlywayProperties flywayProperties;

    public FlywayMigrationManager(FlywayProperties flywayProperties, DataSource dataSource) {
        this.flywayProperties = flywayProperties;
        this.dataSource = dataSource;

    }

    @PostConstruct
    public void ifFlywayMigrationEnable() {
        // Must run after carbornfive migration
        if (flywayProperties.getIsEnableFlywayMigration()) {
            log.debug("FlywayMigrationManager:: Flyway Migrations enabled, running.");
            setupFlyway();
            if (flywayProperties.isRepair()) {
                flyway.repair();
            }
            flyway.migrate();
        } else {
            //do nothing
        }
    }

    private void setupFlyway() {
        this.flyway.setBaselineVersion(MigrationVersion.fromVersion(flywayProperties.getBaseLineVersion()));
        this.flyway.setTable(flywayProperties.getTable());
        this.flyway.setBaselineOnMigrate(flywayProperties.isBaseLineOnMigrate());
        this.flyway.setLocations(flywayProperties.getLocations());
        this.flyway.setSkipDefaultResolvers(flywayProperties.isSkipDefaultResolvers());
        this.flyway.setOutOfOrder(flywayProperties.isOutOfOrder());
        this.flyway.setDataSource(dataSource);
    }
}