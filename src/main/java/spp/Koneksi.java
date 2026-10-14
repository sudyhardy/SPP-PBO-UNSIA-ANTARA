package spp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Koneksi {
    private Koneksi() { }
    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null ? fallback : value;
    }
    public static Connection buka() throws SQLException {
        return DriverManager.getConnection(
            env("DB_URL", "jdbc:mysql://127.0.0.1:3306/dbProjectSiswa?serverTimezone=Asia/Jakarta&connectTimeout=5000&socketTimeout=10000"),
            env("DB_USER", "sppapp"), env("DB_PASSWORD", ""));
    }
}
