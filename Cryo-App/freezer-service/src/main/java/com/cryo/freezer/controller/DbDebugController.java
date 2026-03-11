package com.cryo.freezer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug/db")
public class DbDebugController {

    private static final Logger log = LoggerFactory.getLogger(DbDebugController.class);

    private final DataSource dataSource;

    public DbDebugController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public Map<String, Object> debugDb() throws Exception {
        Map<String, Object> result = new HashMap<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            String url = conn.getMetaData().getURL();
            String catalog = conn.getCatalog();

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM freezer_readings");
            long count = 0;
            if (rs.next()) {
                count = rs.getLong("cnt");
            }
            rs.close();

            result.put("jdbcUrl", url);
            result.put("catalog", catalog);
            result.put("freezerReadingsCount", count);

            log.info("DB DEBUG - url={} catalog={} freezer_readings.count={}", url, catalog, count);
        }

        return result;
    }
}

