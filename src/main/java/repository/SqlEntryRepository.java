package repository;

import database.DatabaseManager;
import model.PokemonEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDate.parse;
import static java.util.stream.Collectors.joining;

public class SqlEntryRepository implements EntryRepository {

    @Override
    public void insert(PokemonEntry entry) {
        String sql = """
                INSERT INTO entries(
                  date, day, time, pokemonName, cp, caught, shiny,
                  weather, park, location, tag, event, incense, incenseDuration
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindEntry(stmt, entry, false);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insertWithId(PokemonEntry entry) {
        String sql = """
                INSERT INTO entries(
                  id, date, day, time, pokemonName, cp, caught, shiny,
                  weather, park, location, tag, event, incense, incenseDuration
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, entry.getId());
            bindEntry(stmt, entry, true);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(PokemonEntry e) {
        String sql = """
                UPDATE entries SET
                  date=?, day=?, time=?, pokemonName=?, cp=?, caught=?, shiny=?,
                  weather=?, park=?, location=?, tag=?, event=?, incense=?, incenseDuration=?
                WHERE id=?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindEntry(stmt, e, false);
            stmt.setInt(15, e.getId());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM entries WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setTagForIds(List<Integer> ids, String tag) {
        if (ids == null || ids.isEmpty()) return;
        String in = ids.stream().map(x -> "?").collect(joining(","));
        String sql = "UPDATE entries SET tag=? WHERE id IN (" + in + ")";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tag);
            for (int i = 0; i < ids.size(); i++) stmt.setInt(i + 2, ids.get(i));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearTagForIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;
        String in = ids.stream().map(x -> "?").collect(joining(","));
        String sql = "UPDATE entries SET tag='' WHERE id IN (" + in + ")";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) stmt.setInt(i + 1, ids.get(i));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // hämtar alla entries utan årsfilter
    @Override
    public List<PokemonEntry> findAll() {
        List<PokemonEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM entries ORDER BY date DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<PokemonEntry> findByPokemonFilterYear(String pokemonFilter, int year) {
        List<PokemonEntry> list = new ArrayList<>();
        boolean hasFilter = pokemonFilter != null && !pokemonFilter.trim().isEmpty();
        String sql = "SELECT * FROM entries WHERE strftime('%Y', date) = ?"
                + (hasFilter ? " AND pokemonName LIKE ?" : "")
                + " ORDER BY date DESC";
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

    @Override
    public List<PokemonEntry> findByPokemonFilterMonth(String pokemonFilter, int year, int month) {
        List<PokemonEntry> list = new ArrayList<>();
        boolean hasFilter = pokemonFilter != null && !pokemonFilter.trim().isEmpty();
        String sql = "SELECT * FROM entries WHERE strftime('%Y', date) = ? AND strftime('%m', date) = ?"
                + (hasFilter ? " AND pokemonName LIKE ?" : "")
                + " ORDER BY date DESC";
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

    @Override
    public List<PokemonEntry> findByTagYear(String tagFilter, int year) {
        List<PokemonEntry> list = new ArrayList<>();
        boolean hasTag = tagFilter != null && !tagFilter.trim().isEmpty();
        String sql = "SELECT * FROM entries WHERE strftime('%Y', date) = ?"
                + (hasTag ? " AND tag LIKE ?" : "")
                + " ORDER BY date DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(year));
            if (hasTag) stmt.setString(2, "%" + tagFilter.trim() + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<PokemonEntry> findByTagMonth(String tagFilter, int year, int month) {
        List<PokemonEntry> list = new ArrayList<>();
        boolean hasTag = tagFilter != null && !tagFilter.trim().isEmpty();
        String sql = "SELECT * FROM entries WHERE strftime('%Y', date) = ? AND strftime('%m', date) = ?"
                + (hasTag ? " AND tag LIKE ?" : "")
                + " ORDER BY date DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, String.valueOf(year));
            stmt.setString(2, String.format("%02d", month));
            if (hasTag) stmt.setString(3, "%" + tagFilter.trim() + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<String> findDistinctTags() {
        List<String> tags = new ArrayList<>();
        String sql = """
                SELECT DISTINCT tag
                FROM entries
                WHERE tag IS NOT NULL AND trim(tag) <> ''
                ORDER BY tag
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String t = rs.getString("tag");
                if (t != null && !t.isBlank()) tags.add(t.trim());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tags;
    }

    // alla unika pokemon i bokstavsordning
    @Override
    public List<String> findDistinctPokemonNames() {
        List<String> names = new ArrayList<>();
        String sql = """
                SELECT DISTINCT pokemonName
                FROM entries
                WHERE pokemonName IS NOT NULL AND trim(pokemonName) <> ''
                ORDER BY pokemonName
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String n = rs.getString("pokemonName");
                if (n != null && !n.isBlank()) names.add(n.trim());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }

    private void bindEntry(PreparedStatement stmt, PokemonEntry e, boolean hasIdAsFirst) throws SQLException {
        int o = hasIdAsFirst ? 2 : 1;
        stmt.setString(o, e.getDate() != null ? e.getDate().toString() : null);
        stmt.setString(o + 1, e.getDay());
        stmt.setString(o + 2, e.getTime());
        stmt.setString(o + 3, e.getPokemonName());
        stmt.setInt(o + 4, e.getCp());
        stmt.setBoolean(o + 5, e.isCaught());
        stmt.setBoolean(o + 6, e.isShiny());
        stmt.setString(o + 7, e.getWeather());
        stmt.setString(o + 8, e.getPark());
        stmt.setString(o + 9, e.getLocation());
        stmt.setString(o + 10, e.getTag());
        stmt.setString(o + 11, e.getEvent());
        stmt.setBoolean(o + 12, e.isIncense());
        stmt.setInt(o + 13, e.getIncenseDuration());
    }

    private PokemonEntry mapRow(ResultSet rs) throws SQLException {
        return new PokemonEntry(
                rs.getInt("id"),
                rs.getString("date") == null ? null : parse(rs.getString("date")),
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
}
