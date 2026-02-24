package service;

import model.PokemonEntry;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import util.TextNormalizer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelService {

    private static final ExcelService INSTANCE = new ExcelService();

    public static ExcelService getInstance() {
        return INSTANCE;
    }

    public void exportEntries(Path file, List<PokemonEntry> entries) throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Entries");

            Row h = sheet.createRow(0);
            String[] headers = {
                    "Id", "Date", "Day", "Time", "Pokemon", "CP", "Caught", "Shiny",
                    "Weather", "Park", "Location", "Tag", "Event", "Incense", "IncenseDuration"
            };
            for (int i = 0; i < headers.length; i++) h.createCell(i).setCellValue(headers[i]);

            int r = 1;
            for (PokemonEntry e : entries) {
                Row row = sheet.createRow(r++);
                int c = 0;

                row.createCell(c++).setCellValue(e.getId());
                row.createCell(c++).setCellValue(e.getDate() != null ? e.getDate().toString() : "");
                row.createCell(c++).setCellValue(nz(e.getDay()));
                row.createCell(c++).setCellValue(nz(e.getTime()));
                row.createCell(c++).setCellValue(nz(e.getPokemonName()));
                row.createCell(c++).setCellValue(e.getCp());
                row.createCell(c++).setCellValue(e.isCaught() ? "Yes" : "No");
                row.createCell(c++).setCellValue(e.isShiny() ? "Yes" : "No");
                row.createCell(c++).setCellValue(nz(e.getWeather()));
                row.createCell(c++).setCellValue(nz(e.getPark()));
                row.createCell(c++).setCellValue(nz(e.getLocation()));
                row.createCell(c++).setCellValue(nz(e.getTag()));
                row.createCell(c++).setCellValue(nz(e.getEvent()));
                row.createCell(c++).setCellValue(e.isIncense() ? "Yes" : "No");
                row.createCell(c++).setCellValue(e.getIncenseDuration());
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream out = new FileOutputStream(file.toFile())) {
                wb.write(out);
            }
        }
    }

    public ImportResult importEntries(Path file) throws Exception {
        int inserted = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        try (Workbook wb = WorkbookFactory.create(new FileInputStream(file.toFile()))) {

            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                Sheet sheet = wb.getSheetAt(s);
                if (sheet.getPhysicalNumberOfRows() < 2) continue;

                Row header = sheet.getRow(0);
                if (header == null) continue;

                Map<String, Integer> col = buildHeaderMap(header);
                if (!col.containsKey("date")) continue;

                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;

                    boolean allBlank = true;
                    for (Cell cell : row) {
                        if (cell != null && cell.getCellType() != CellType.BLANK
                                && !cell.toString().trim().isEmpty()) {
                            allBlank = false;
                            break;
                        }
                    }
                    if (allBlank) continue;

                    try {
                        LocalDate date = readDate(row, col, "date");
                        if (date == null) {
                            skipped++;
                            errors.add("Row " + (r + 1) + ": missing date, skipped.");
                            continue;
                        }

                        String day = readString(row, col, "day");
                        if (day.isBlank() && date != null)
                            day = capitalize(date.getDayOfWeek().toString());

                        String time = readString(row, col, "time");
                        String weather = normalizeWeather(readString(row, col, "weather"));
                        String park = readString(row, col, "park");
                        String location = readString(row, col, "location");

                        //försök hitta pokémon från olika möjliga kolumnnamn
                        String pokemon = readString(row, col, "pokemon");
                        if (pokemon.isBlank()) pokemon = readString(row, col, "bird");
                        if (pokemon.isBlank()) pokemon = readString(row, col, "name");
                        pokemon = TextNormalizer.smartTitle(pokemon);

                        int cp = readInt(row, col, "cp");
                        boolean shiny = readBoolean(row, col, "shiny");
                        boolean caught = readBoolean(row, col, "caught");

                        String tag = TextNormalizer.smartTitle(readString(row, col, "tag"));
                        String event = readString(row, col, "event");

                        boolean incense = readBoolean(row, col, "incense");
                        int incenseDuration = readInt(row, col, "incenseduration");

                        PokemonEntry entry = new PokemonEntry(
                                date, day, time, pokemon, cp, caught, shiny,
                                weather, park, location, tag, event, incense, incenseDuration
                        );

                        EntryService.getInstance().add(entry);
                        inserted++;

                    } catch (Exception ex) {
                        skipped++;
                        errors.add("Row " + (r + 1) + ": " + ex.getMessage());
                    }
                }
            }
        }

        return new ImportResult(inserted, skipped, errors);
    }

    public record ImportResult(int inserted, int skipped, List<String> errors) {
    }

    private Map<String, Integer> buildHeaderMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            String h = cell.toString();
            if (h == null) continue;
            String key = h.trim().toLowerCase();

            key = switch (key) {
                case "pokemonname", "pokemon", "pokémon", "pokémon name" -> "pokemon";
                case "bird" -> "bird";
                case "name" -> "name";
                case "date" -> "date";
                case "day", "weekday" -> "day";
                case "time", "start time" -> "time";
                case "weather" -> "weather";
                case "park" -> "park";
                case "location" -> "location";
                case "cp" -> "cp";
                case "caught" -> "caught";
                case "shiny" -> "shiny";
                case "tag", "tags" -> "tag";
                case "event" -> "event";
                case "incense" -> "incense";
                case "incense duration", "duration", "incenseduration" -> "incenseduration";
                default -> key.replaceAll("\\s+", "");
            };

            map.put(key, cell.getColumnIndex());
        }
        return map;
    }

    private LocalDate readDate(Row row, Map<String, Integer> col, String key) {
        Integer idx = col.get(key);
        if (idx == null) return null;
        Cell cell = row.getCell(idx);
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }

        String s = cell.toString().trim();
        if (s.isBlank()) return null;

        try {
            return LocalDate.parse(s);
        } catch (Exception ignored) {
        }

        for (String pattern : List.of("M/d/yyyy", "MM/dd/yyyy", "d/M/yyyy", "dd/MM/yyyy")) {
            try {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String readString(Row row, Map<String, Integer> col, String key) {
        Integer idx = col.get(key);
        if (idx == null) return "";
        Cell cell = row.getCell(idx);
        if (cell == null) return "";
        return cell.toString().trim();
    }

    private int readInt(Row row, Map<String, Integer> col, String key) {
        Integer idx = col.get(key);
        if (idx == null) return 0;
        Cell cell = row.getCell(idx);
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) return (int) Math.round(cell.getNumericCellValue());
        String s = cell.toString().trim();
        if (s.isBlank()) return 0;
        try {
            return (int) Math.round(Double.parseDouble(s));
        } catch (Exception ignored) {
        }
        return 0;
    }

    private boolean readBoolean(Row row, Map<String, Integer> col, String key) {
        Integer idx = col.get(key);
        if (idx == null) return false;
        Cell cell = row.getCell(idx);
        if (cell == null) return false;
        String s = cell.toString().trim().toLowerCase();
        return s.equals("yes") || s.equals("true") || s.equals("1") || s.equals("y");
    }

    private String normalizeWeather(String w) {
        if (w == null) return "";
        String t = w.trim();
        if (t.equalsIgnoreCase("sunny") || t.equalsIgnoreCase("clear")) return "Sunny/Clear";
        return TextNormalizer.smartTitle(t);
    }

    //gör THURSDAY till Thursday
    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }
}
