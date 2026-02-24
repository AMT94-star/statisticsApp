package database;

import model.PokemonEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EntryDAO {

    public static void insertEntry(PokemonEntry entry) {
        String sql = """
                INSERT INTO entries(
                  date, day, time, pokemonName, cp, caught, shiny,
                  weather, park, location, tag, event, incense, incenseDuration
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entry.getDate() != null ? entry.getDate().toString() : null);
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

    public static void updateEntry(PokemonEntry e) {
        String sql = """
                UPDATE entries SET
                  date=?, day=?, time=?, pokemonName=?, cp=?, caught=?, shiny=?,
                  weather=?, park=?, location=?, tag=?, event=?, incense=?, incenseDuration=?
                WHERE id=?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, e.getDate() != null ? e.getDate().toString() : null);
            stmt.setString(2, e.getDay());
            stmt.setString(3, e.getTime());
            stmt.setString(4, e.getPokemonName());
            stmt.setInt(5, e.getCp());
            stmt.setBoolean(6, e.isCaught());
            stmt.setBoolean(7, e.isShiny());
            stmt.setString(8, e.getWeather());
            stmt.setString(9, e.getPark());
            stmt.setString(10, e.getLocation());
            stmt.setString(11, e.getTag());
            stmt.setString(12, e.getEvent());
            stmt.setBoolean(13, e.isIncense());
            stmt.setInt(14, e.getIncenseDuration());
            stmt.setInt(15, e.getId());

            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void deleteById(int id) {
        String sql = "DELETE FROM entries WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static List<PokemonEntry> getByFilterAndYear(String pokemonFilter, int year) {
        List<PokemonEntry> list = new ArrayList<>();
        boolean hasFilter = pokemonFilter != null && !pokemonFilter.trim().isEmpty();

        String sql = """
                SELECT * FROM entries
                WHERE strftime('%Y', date) = ?
                """ + (hasFilter ? " AND pokemonName LIKE ? " : "") + """
                ORDER BY date DESC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, String.valueOf(year));
            if (hasFilter) stmt.setString(2, "%" + pokemonFilter.trim() + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<PokemonEntry> getByFilterAndMonth(String pokemonFilter, int year, int month) {
        List<PokemonEntry> list = new ArrayList<>();
        boolean hasFilter = pokemonFilter != null && !pokemonFilter.trim().isEmpty();

        String sql = """
                SELECT * FROM entries
                WHERE strftime('%Y', date) = ?
                  AND strftime('%m', date) = ?
                """ + (hasFilter ? " AND pokemonName LIKE ? " : "") + """
                ORDER BY date DESC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, String.valueOf(year));
            stmt.setString(2, String.format("%02d", month));
            if (hasFilter) stmt.setString(3, "%" + pokemonFilter.trim() + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // mappar en DB-rad till objekt
    private static PokemonEntry mapRow(ResultSet rs) throws SQLException {
        return new PokemonEntry(
                rs.getInt("id"),
                rs.getString("date") == null ? null : LocalDate.parse(rs.getString("date")),
                rs.getString("day"),
                rs.getString("time"),
                rs.getString("pokemonName"),
                rs.getInt("cp"),
                rs.getBoolean("caught"),
                rs.getBoolean("shiny"),
                rs.getString("weather"),
                rs.getString("park"),
                rs.getString("location"),
                rs.getString("tag"),
                rs.getString("event"),
                rs.getBoolean("incense"),
                rs.getInt("incenseDuration")
        );
    }

    public static void insertEntryWithId(PokemonEntry entry) {
        String sql = """
                INSERT INTO entries(
                  id, date, day, time, pokemonName, cp, caught, shiny,
                  weather, park, location, tag, event, incense, incenseDuration
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, entry.getId());
            stmt.setString(2, entry.getDate() != null ? entry.getDate().toString() : null);
            stmt.setString(3, entry.getDay());
            stmt.setString(4, entry.getTime());
            stmt.setString(5, entry.getPokemonName());
            stmt.setInt(6, entry.getCp());
            stmt.setBoolean(7, entry.isCaught());
            stmt.setBoolean(8, entry.isShiny());
            stmt.setString(9, entry.getWeather());
            stmt.setString(10, entry.getPark());
            stmt.setString(11, entry.getLocation());
            stmt.setString(12, entry.getTag());
            stmt.setString(13, entry.getEvent());
            stmt.setBoolean(14, entry.isIncense());
            stmt.setInt(15, entry.getIncenseDuration());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<PokemonEntry> getByTagAndYear(String tagFilter, int year) {
        List<PokemonEntry> list = new ArrayList<>();
        boolean hasTag = tagFilter != null && !tagFilter.trim().isEmpty();

        String sql = """
                    SELECT * FROM entries
                    WHERE strftime('%Y', date) = ?
                """ + (hasTag ? " AND lower(tag) = lower(?) " : "") + """
                    ORDER BY date DESC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, String.valueOf(year));
            if (hasTag) stmt.setString(2, tagFilter.trim());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<PokemonEntry> getByTagAndMonth(String tagFilter, int year, int month) {
        List<PokemonEntry> list = new ArrayList<>();
        boolean hasTag = tagFilter != null && !tagFilter.trim().isEmpty();

        String sql = """
                    SELECT * FROM entries
                    WHERE strftime('%Y', date) = ?
                      AND strftime('%m', date) = ?
                """ + (hasTag ? " AND lower(tag) = lower(?) " : "") + """
                    ORDER BY date DESC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, String.valueOf(year));
            stmt.setString(2, String.format("%02d", month));
            if (hasTag) stmt.setString(3, tagFilter.trim());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<String> getAllDistinctTags() {
        List<String> tags = new ArrayList<>();

        String sql = """
                    SELECT DISTINCT lower(trim(tag)) AS t
                    FROM entries
                    WHERE tag IS NOT NULL AND trim(tag) <> ''
                    ORDER BY t
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String t = rs.getString("t");
                if (t != null && !t.isBlank()) tags.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tags;
    }
}