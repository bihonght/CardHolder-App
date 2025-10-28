package com.cardpayment.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public final class Database {
    private static HikariDataSource ds;

    public static void init(String url, String user, String pass) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(user);
        cfg.setPassword(pass);
        cfg.setMaximumPoolSize(5);
        cfg.setMinimumIdle(1);
        cfg.setPoolName("cardpayment-pool");
        ds = new HikariDataSource(cfg);
    }

    // keep the original init() but forward to the overload:
    public static void init() {
        String url  = getenvOr("DB_URL",  "jdbc:postgresql://localhost:5432/carddb");
        String user = getenvOr("DB_USER", "carduser");
        String pass = getenvOr("DB_PASS", "cardpass");
        init(url, user, pass);
    }

    public static Connection get() throws Exception {
        return ds.getConnection();
    }

    public static void ensureSchema(Connection c) throws Exception {
        try (Statement st = c.createStatement()) {
            String sql = readResource("/schema.sql");
            st.execute(sql);
        }
    }

    private static String readResource(String path) throws Exception {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Database.class.getResourceAsStream(path), StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    private static String getenvOr(String k, String d) {
        String v = System.getenv(k);
        return (v == null || v.isEmpty()) ? d : v;
    }
}
