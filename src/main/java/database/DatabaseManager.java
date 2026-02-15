package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:pokemon_stats.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {

        String sql = """
                CREATE TABLE IF NOT EXISTS entries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT,
                    day TEXT,
                    time TEXT,
                    pokemonName TEXT,
                    cp INTEGER,
                    caught BOOLEAN,
                    shiny BOOLEAN,
                    weather TEXT,
                    park TEXT,
                    location TEXT,
                    tag TEXT,
                    event TEXT,
                    incense BOOLEAN,
                    incenseDuration INTEGER
                );
                """;

        try (Connection conn = getConnection();
             var stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("Database ready!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}