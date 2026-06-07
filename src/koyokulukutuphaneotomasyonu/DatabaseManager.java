package koyokulukutuphaneotomasyonu;

import java.sql.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:kutuphane.db";

   public static Connection getConnection() throws SQLException {
    try {
        Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
        throw new SQLException("SQLite JDBC driver bulunamadı.", e);
    }

    return DriverManager.getConnection(DB_URL);
}

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // USERS tablosu
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL CHECK(role IN ('teacher', 'student')),
                    student_number TEXT
                )
            """);

            // BOOKS tablosu
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS books (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    author TEXT NOT NULL,
                    category TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'available' CHECK(status IN ('available', 'borrowed'))
                )
            """);

            // LOANS tablosu
            stmt.execute("""
    CREATE TABLE IF NOT EXISTS loans (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        book_id INTEGER,
        user_id INTEGER NOT NULL,
        book_title TEXT,
        loan_date TEXT NOT NULL,
        due_date TEXT NOT NULL,
        return_date TEXT,
        FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE SET NULL,
        FOREIGN KEY (user_id) REFERENCES users(id)
    )
""");

            // Varsayılan öğretmen hesabı 
            stmt.execute("""
                INSERT OR IGNORE INTO users (name, username, password, role)
                VALUES ('Yönetici Öğretmen', 'admin', 'admin123', 'teacher')
            """);

            System.out.println("Veritabanı başarıyla başlatıldı.");

        } catch (SQLException e) {
            System.err.println("Veritabanı hatası: " + e.getMessage());
        }
    }
}