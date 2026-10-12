package com.jawla.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseMigrationConfig {

    @Bean
    ApplicationRunner widenGoogleMapsUrlColumn(JdbcTemplate jdbcTemplate) {
        return args -> {
            String type = jdbcTemplate.query(
                "SELECT DATA_TYPE FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store' AND COLUMN_NAME = 'maps_url'",
                rs -> rs.next() ? rs.getString(1) : null
            );

            // Old deployments used VARCHAR(500), which rejects long Google Maps links.
            // Only migrate old schemas; TEXT data stays intact on future restarts.
            if ("varchar".equalsIgnoreCase(type)) {
                jdbcTemplate.execute("ALTER TABLE store MODIFY COLUMN maps_url TEXT NULL");
            }
        };
    }
}
