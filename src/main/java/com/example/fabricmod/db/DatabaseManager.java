package com.example.fabricmod.db;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public enum DatabaseManager {
    INSTANCE;
    private static final String URL = "jdbc:sqlite:player.data";
    private Connection conn;

    public void init() {
        try {
            conn = DriverManager.getConnection(URL);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(""" 
                    CREATE TABLE IF NOT EXISTS transactions (
                      id INTEGER PRIMARY KEY,
                      username TEXT,
                      item TEXT,
                      rarity TEXT,
                      number INTEGER,
                      cert TEXT,
                      timestamp TEXT
                    );
                """);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logTransaction(String username, String item, String rarity, int number, String cert) {
        if (conn == null) return;
        String sql = "INSERT INTO transactions (username,item,rarity,number,cert,timestamp) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, item);
            ps.setString(3, rarity);
            ps.setInt(4, number);
            ps.setString(5, cert);
            ps.setString(6, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
