package ui.charts;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import model.PokemonEntry;

import java.time.LocalDate;
import java.util.*;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.lang.String.format;
import static java.util.stream.Collectors.*;
import static javafx.scene.control.Tooltip.install;

public class ChartBuilders {

    //palett
    private static final Color[] PALETTE = {
            Color.web("#3b82f6"), Color.web("#10b981"), Color.web("#f59e0b"),
            Color.web("#ef4444"), Color.web("#8b5cf6"), Color.web("#06b6d4"),
            Color.web("#f97316"), Color.web("#ec4899")
    };

    //vecko pie
    public static Node dayOfWeekPie(FilteredList<PokemonEntry> filtered) {
        List<String> order = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String d : order) counts.put(d, 0L);

        for (PokemonEntry e : filtered) {
            String day = safe(e.getDay()).trim();
            if (day.isBlank()) continue;
            String key = capitalize(day);
            if (counts.containsKey(key)) counts.put(key, counts.get(key) + 1);
        }
        counts.values().removeIf(v -> v == 0);

        return buildPie("Day of Week", counts);
    }

    //catch rate bar
    public static Node catchRateBar(FilteredList<PokemonEntry> filtered) {
        List<PokemonEntry> entries = new ArrayList<>(filtered);

        Map<String, Long> caught = entries.stream()
                .filter(e -> !safe(e.getPokemonName()).isBlank())
                .collect(groupingBy(e -> safe(e.getPokemonName()).trim(),
                        filtering(PokemonEntry::isCaught, counting())));

        Map<String, Long> notCaught = entries.stream()
                .filter(e -> !safe(e.getPokemonName()).isBlank())
                .collect(groupingBy(e -> safe(e.getPokemonName()).trim(),
                        filtering(e -> !e.isCaught(), counting())));

        Map<String, Long> totals = new HashMap<>();
        Set<String> keys = new HashSet<>();
        keys.addAll(caught.keySet());
        keys.addAll(notCaught.keySet());
        for (String k : keys) totals.put(k, caught.getOrDefault(k, 0L) + notCaught.getOrDefault(k, 0L));

        List<String> top = totals.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8).map(Map.Entry::getKey).toList();

        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        y.setLabel("Count");
        StackedBarChart<String, Number> chart = new StackedBarChart<>(x, y);
        chart.setTitle("Catch Rate");
        chart.setLegendVisible(true);
        chart.setAnimated(false);

        XYChart.Series<String, Number> sCaught = new XYChart.Series<>();
        sCaught.setName("Caught");
        XYChart.Series<String, Number> sNot = new XYChart.Series<>();
        sNot.setName("Not caught");

        for (String p : top) {
            long c = caught.getOrDefault(p, 0L);
            long nc = notCaught.getOrDefault(p, 0L);
            var d1 = new XYChart.Data<String, Number>(p, c);
            var d2 = new XYChart.Data<String, Number>(p, nc);
            sCaught.getData().add(d1);
            sNot.getData().add(d2);
            d1.nodeProperty().addListener((ob, o, n) -> {
                if (n != null) install(n, new Tooltip(p + " caught: " + c));
            });
            d2.nodeProperty().addListener((ob, o, n) -> {
                if (n != null) install(n, new Tooltip(p + " not caught: " + nc));
            });
        }
        chart.getData().addAll(sCaught, sNot);
        x.setTickLabelRotation(20);
        return sized(chart, 260);
    }

    //shiny pie
    public static Node shinyPie(FilteredList<PokemonEntry> filtered) {
        List<PokemonEntry> entries = new ArrayList<>(filtered);
        long shiny = entries.stream().filter(PokemonEntry::isShiny).count();
        long not = entries.size() - shiny;
        double rate = entries.isEmpty() ? 0 : (double) shiny / entries.size() * 100;

        Label pctLabel = new Label(format("%.2f%%", rate));
        pctLabel.setStyle("-fx-font-size: 16px; " +
                "-fx-font-weight: 700; -fx-text-fill: #f59e0b;");

        Node pie = buildPie("Shiny vs Not Shiny", Map.of(
                "Shiny ✨", shiny, "Not shiny", not));

        VBox box = new VBox(6, pctLabel, pie);
        box.setAlignment(Pos.TOP_LEFT);
        return box;
    }

    //väder pie
    public static Node weatherPie(FilteredList<PokemonEntry> filtered) {
        Map<String, Long> counts = new ArrayList<>(filtered).stream()
                .map(e -> safe(e.getWeather()).trim())
                .filter(w -> !w.isBlank())
                .collect(groupingBy(w -> w, counting()));
        return buildPie("Weather Conditions", counts);
    }

    //park och location
    public static Node parkLocationStacked(FilteredList<PokemonEntry> filtered) {
        List<PokemonEntry> entries = new ArrayList<>(filtered);
        Map<String, Map<String, Long>> parkToLoc = new HashMap<>();

        for (PokemonEntry e : entries) {
            String park = safe(e.getPark()).trim();
            String loc = safe(e.getLocation()).trim();
            if (park.isBlank()) park = "Unknown";
            if (loc.isBlank()) loc = "Unknown";
            parkToLoc.computeIfAbsent(park, k -> new HashMap<>());
            Map<String, Long> m = parkToLoc.get(park);
            m.put(loc, m.getOrDefault(loc, 0L) + 1);
        }

        List<String> parks = parkToLoc.entrySet().stream()
                .sorted((a, b) -> Long.compare(sum(b.getValue()), sum(a.getValue())))
                .map(Map.Entry::getKey).toList();

        Set<String> locs = new TreeSet<>(CASE_INSENSITIVE_ORDER);
        for (var m : parkToLoc.values()) locs.addAll(m.keySet());

        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        y.setLabel("Entries");
        StackedBarChart<String, Number> chart = new StackedBarChart<>(x, y);
        chart.setTitle("Location");
        chart.setAnimated(false);
        chart.setLegendVisible(true);
        x.getCategories().setAll(parks);
        x.setTickLabelRotation(30);

        for (String loc : locs) {
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.setName(loc);
            for (String park : parks) {
                long v = parkToLoc.getOrDefault(park, Map.of()).getOrDefault(loc, 0L);
                var d = new XYChart.Data<String, Number>(park, v);
                s.getData().add(d);
                final String pf = park;
                final long vf = v;
                d.nodeProperty().addListener((ob, o, n) -> {
                    if (n != null) install(n, new Tooltip(pf + "\n" + loc + ": " + vf));
                });
            }
            chart.getData().add(s);
        }
        return sized(chart, 280);
    }

    //incense linje
    public static Node incenseLine(FilteredList<PokemonEntry> filtered) {
        Map<LocalDate, List<PokemonEntry>> byDay = new ArrayList<>(filtered).stream()
                .filter(e -> e.getDate() != null && e.isIncense())
                .collect(groupingBy(PokemonEntry::getDate));

        Map<LocalDate, Double> avg = byDay.entrySet().stream()
                .collect(toMap(Map.Entry::getKey,
                        e -> e.getValue().stream().mapToInt(
                                PokemonEntry::getIncenseDuration).average().orElse(0),
                        (a, b) -> a, TreeMap::new));

        return buildLine("Incense Duration", avg, "Avg minutes");
    }

    //avg cp
    public static Node avgCpLine(FilteredList<PokemonEntry> filtered) {
        Map<LocalDate, List<PokemonEntry>> byDay = new ArrayList<>(filtered).stream()
                .filter(e -> e.getDate() != null && e.getCp() > 0)
                .collect(groupingBy(PokemonEntry::getDate));

        Map<LocalDate, Double> avg = byDay.entrySet().stream()
                .collect(toMap(Map.Entry::getKey,
                        e -> e.getValue().stream().mapToInt(PokemonEntry::getCp).average().orElse(0),
                        (a, b) -> a, TreeMap::new));

        return buildLine("Avg CP over Time", avg, "Avg CP");
    }

    //top pokies
    public static Node topPokemonBar(FilteredList<PokemonEntry> filtered) {
        Map<String, Long> counts = new ArrayList<>(filtered).stream()
                .map(e -> safe(e.getPokemonName()).trim())
                .filter(n -> !n.isBlank())
                .collect(groupingBy(n -> n, counting()));

        List<Map.Entry<String, Long>> top = counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10).toList();

        CategoryAxis y = new CategoryAxis();
        NumberAxis x = new NumberAxis();
        x.setLabel("Encounters");
        BarChart<Number, String> chart = new BarChart<>(x, y);
        chart.setTitle("Top Pokémon");
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        XYChart.Series<Number, String> s = new XYChart.Series<>();
        for (var e : top) s.getData().add(new XYChart.Data<>(e.getValue(), e.getKey()));
        chart.getData().add(s);
        chart.setPrefHeight(Math.max(200, top.size() * 30));
        return chart;
    }

    //encounters per day
    public static Node encountersLine(FilteredList<PokemonEntry> filtered) {
        Map<LocalDate, Long> counts = new ArrayList<>(filtered).stream()
                .filter(e -> e.getDate() != null)
                .collect(groupingBy(PokemonEntry::getDate, counting()));

        Map<LocalDate, Double> asDouble = new TreeMap<>();
        counts.forEach((k, v) -> asDouble.put(k, (double) v));
        return buildLine("Encounters per Day", asDouble, "Count");
    }

    //tag pie
    public static Node tagPie(FilteredList<PokemonEntry> filtered) {
        Map<String, Long> counts = new ArrayList<>(filtered).stream()
                .map(e -> safe(e.getTag()).trim())
                .map(t -> t.isBlank() ? "Untagged" : t)
                .collect(groupingBy(t -> t, counting()));
        return buildPie("Tag Distribution", counts);
    }

    //bubble chart
    public static Node pokemonBubble(FilteredList<PokemonEntry> filtered) {
        Map<String, Long> caught = new ArrayList<>(filtered).stream()
                .filter(PokemonEntry::isCaught)
                .filter(e -> !safe(e.getPokemonName()).isBlank())
                .collect(groupingBy(e -> safe(e.getPokemonName()).trim(), counting()));

        List<Map.Entry<String, Long>> sorted = caught.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()).toList();

        NumberAxis x = new NumberAxis();
        NumberAxis y = new NumberAxis();
        y.setLabel("Caught");
        BubbleChart<Number, Number> chart = new BubbleChart<>(x, y);
        chart.setTitle("Pokémon Bubbles");
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        XYChart.Series<Number, Number> s = new XYChart.Series<>();
        for (int i = 0; i < sorted.size(); i++) {
            String name = sorted.get(i).getKey();
            long cnt = sorted.get(i).getValue();
            double sz = Math.max(1, Math.sqrt(cnt) * 2);
            var d = new XYChart.Data<Number, Number>(i + 1, cnt, sz);
            s.getData().add(d);
            d.nodeProperty().addListener((ob, o, n) -> {
                if (n != null) install(n, new Tooltip(name + " — caught: " + cnt));
            });
        }
        chart.getData().add(s);
        return sized(chart, 260);
    }

    private static Node buildPie(String title, Map<String, Long> counts) {
        List<Map.Entry<String, Long>> sorted = counts.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .toList();

        PieChart pie = new PieChart();
        pie.setTitle(title);
        pie.setLegendVisible(false);
        pie.setLabelsVisible(false);
        pie.setAnimated(false);
        pie.setPrefHeight(180);
        pie.setMinHeight(180);

        long sum = sorted.stream().mapToLong(Map.Entry::getValue).sum();
        List<PieChart.Data> dataList = new ArrayList<>();

        for (var e : sorted) {
            PieChart.Data d = new PieChart.Data(e.getKey(), e.getValue());
            pie.getData().add(d);
            dataList.add(d);
            double pct = sum == 0 ? 0 : 100.0 * e.getValue() / sum;
            String tip = e.getKey() + ": " + e.getValue() + " (" + format("%.1f%%", pct) + ")";
            d.nodeProperty().addListener((ob, o, n) -> {
                if (n != null) install(n, new Tooltip(tip));
            });
        }

        // legends,2 kolumner
        GridPane legend = new GridPane();
        legend.setHgap(12);
        legend.setVgap(5);
        legend.setPadding(new Insets(4, 0, 0, 0));

        Runnable buildLegend = () -> {
            legend.getChildren().clear();
            for (int i = 0; i < dataList.size(); i++) {
                PieChart.Data d = dataList.get(i);
                long v = (long) d.getPieValue();
                double pct = sum == 0 ? 0 : 100.0 * v / sum;

                Color c = readSliceColor(d);
                if (c == null) c = PALETTE[i % PALETTE.length];

                Region swatch = new Region();
                swatch.setPrefSize(10, 10);
                swatch.setMinSize(10, 10);
                swatch.setMaxSize(10, 10);
                swatch.setStyle("-fx-background-color: " + toHex(c) + "; " +
                        "-fx-background-radius: 2;");

                Label lbl = new Label(d.getName() + " " + format("(%.1f%%)", pct));
                lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
                lbl.setWrapText(true);
                lbl.setMaxWidth(180);

                HBox item = new HBox(6, swatch, lbl);
                item.setAlignment(Pos.CENTER_LEFT);
                legend.add(item, i % 2, i / 2);
            }
        };

        Platform.runLater(buildLegend);
        PauseTransition pt = new PauseTransition(Duration.millis(80));
        pt.setOnFinished(e -> buildLegend.run());
        pt.play();

        return new VBox(4, pie, legend);
    }

    private static Node buildLine(String title, Map<LocalDate, Double> series, String yLabel) {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        y.setLabel(yLabel);
        LineChart<String, Number> chart = new LineChart<>(x, y);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(series.size() <= 30);
        chart.setAnimated(false);

        XYChart.Series<String, Number> s = new XYChart.Series<>();
        series.forEach((day, val) -> {
            var d = new XYChart.Data<String, Number>(day.toString(), val);
            s.getData().add(d);
            d.nodeProperty().addListener((ob, o, n) -> {
                if (n != null) install(n, new Tooltip(day + ": " + format("%.1f", val)));
            });
        });
        chart.getData().add(s);
        x.setTickLabelRotation(40);
        return sized(chart, 220);
    }

    private static Node sized(Node n, double h) {
        if (n instanceof Chart c) {
            c.setPrefHeight(h);
            c.setMinHeight(h);
        }
        if (n instanceof Region r) {
            r.setPrefHeight(h);
            r.setMinHeight(h);
        }
        return n;
    }

    private static Color readSliceColor(PieChart.Data d) {
        if (d == null || d.getNode() == null) return null;
        if (d.getNode() instanceof Path p) {
            Paint paint = p.getFill();
            if (paint instanceof Color c) return c;
        }
        if (d.getNode() instanceof Region r) {
            Background bg = r.getBackground();
            if (bg != null && !bg.getFills().isEmpty()) {
                Paint fill = bg.getFills().get(0).getFill();
                if (fill instanceof Color c) return c;
            }
        }
        return null;
    }

    private static String toHex(Color c) {
        return format("#%02x%02x%02x",
                (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private static long sum(Map<String, Long> m) {
        return m.values().stream().mapToLong(Long::longValue).sum();
    }
}
