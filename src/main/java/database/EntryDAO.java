package database;

import model.PokemonEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class EntryDAO {

    public static void insertEntry(PokemonEntry entry) {

        String sql = """
                INSERT INTO entries(
                date, day, time, pokemonName, cp, caught, shiny,
                weather, park, location, tag, event, incense, incenseDuration)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entry.getDate().toString());
            stmt.setString(2, entry.getDay());
            stmt.setString(3, entry.getTime());
            stmt.setString(4, entry.getPokemonName());
            stmt.setInt(5, entry.getCp());
            stmt.setBoolean(6, entry.isCaught());
            stmt.setBoolean(7, entry.isShiny());
            stmt.setString(8, entry.getWeather());
            stmt.setString(9, entry.getPark());
            stmt.setString(10, entry.getLocation());
            stmt.setString(11, entry.getTag());
            stmt.setString(12, entry.getEvent());
            stmt.setBoolean(13, entry.isIncense());
            stmt.setInt(14, entry.getIncenseDuration());

            stmt.executeUpdate();
            System.out.println("Entry saved!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}