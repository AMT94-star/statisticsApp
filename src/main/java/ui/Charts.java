package ui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import model.PokemonEntry;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Charts {

    public static Node makeChartsForFilteredList(FilteredList<PokemonEntry> filtered, boolean tagMode) {

        VBox content = new VBox(12);
        content.setPadding(new Insets(10));
        content.getStyleClass().add("card");

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Runnable rebuild = () -> rebuildDashboard(content, filtered, tagMode);

        rebuild.run();
        filtered.addListener((ListChangeListener<PokemonEntry>) c -> rebuild.run());

        return scroll;
    }

    private static void rebuildDashboard(VBox root, FilteredList<PokemonEntry> filtered, boolean tagMode) {
        root.getChildren().clear();

        List<PokemonEntry> entries = new ArrayList<>(filtered);
        int total = entries.size();

        long shiny = entries.stream().filter(PokemonEntry::isShiny).count();
        long notShiny = total - shiny;
        double shinyRate = total == 0 ? 0 : (double) shiny / total;

        //weather counts
        Map<String, Long> weatherCounts = entries.stream()
                .map(e -> safe(e.getWeather()).trim())
                .map(w -> w.isBlank() ? "Unknown" : w)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        //catch split per pokie
        Map<String, Long> caughtByPokemon = entries.stream()
                .filter(e -> !safe(e.getPokemonName()).trim().isBlank())
                .collect(Collectors.groupingBy(
                        e -> safe(e.getPokemonName()).trim(),
                        Collectors.filtering(PokemonEntry::isCaught, Collectors.counting())
                ));

        Map<String, Long> notCaughtByPokemon = entries.stream()
                .filter(e -> !safe(e.getPokemonName()).trim().isBlank())
                .collect(Collectors.groupingBy(
                        e -> safe(e.getPokemonName()).trim(),
                        Collectors.filtering(e -> !e.isCaught(), Collectors.counting())
                ));

        //by day
        Map<LocalDate, List<PokemonEntry>> byDay = entries.stream()
                .collect(Collectors.groupingBy(PokemonEntry::getDate));
        byDay.remove(null);

        Map<LocalDate, Double> avgCpByDay = byDay.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().mapToInt(PokemonEntry::getCp).average().orElse(0),
                        (a, b) -> a,
                        TreeMap::new
                ));

        Map<LocalDate, Double> incenseAvgByDay = byDay.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            List<PokemonEntry> list = e.getValue().stream().filter(PokemonEntry::isIncense).toList();
                            if (list.isEmpty()) return 0.0;
                            return list.stream().mapToInt(PokemonEntry::getIncenseDuration).average().orElse(0);
                        },
                        (a, b) -> a,
                        TreeMap::new
                ));

        //total entries pie, week
        Map<String, Long> dow = entriesByDayOfWeek(entries);

        Label title = new Label(tagMode ? "Tag charts" : "Charts");
        title.getStyleClass().add("section-title");
        root.getChildren().add(title);

        //total entries pie
        root.getChildren().add(chartCard(sized(
                pieWithCustomLegend("Total entries (Day of Week)", dow),
                360
        )));

        //catch rate
        root.getChildren().add(chartCard(sized(
                stackedCatchRateByPokemon(caughtByPokemon, notCaughtByPokemon, 10),
                360
        )));

        //shiny pie + %
        root.getChildren().add(chartCard(sized(
                shinyPieWithBigPercent(shiny, notShiny, shinyRate),
                320
        )));

        //weather pie
        root.getChildren().add(chartCard(sized(
                pieWithCustomLegend("Weather conditions", weatherCounts),
                360
        )));

        //park + location
        root.getChildren().add(chartCard(sized(
                stackedParkLocation(entries),
                440
        )));

        //incense duration line
        root.getChildren().add(chartCard(sized(
                lineWithMarkers("Incense duration (avg minutes per day)", incenseAvgByDay, "Avg minutes"),
                320
        )));

        //avg CP
        root.getChildren().add(chartCard(sized(
                lineWithMarkers("Avg CP (per day)", avgCpByDay, "Avg CP"),
                320
        )));

        //tag mode, bubble chart används dock inte än
        if (tagMode) {
            root.getChildren().add(chartCard(sized(
                    pokemonBubblesByCaught(caughtByPokemon),
                    420
            )));
        }
    }

    private static Node chartCard(Node inner) {
        VBox box = new VBox(inner);
        box.setPadding(new Insets(10));
        box.setSpacing(6);
        box.getStyleClass().add("card");
        return box;
    }

    private static Node sized(Node n, double prefH) {
        if (n instanceof Chart c) {
            c.setAnimated(false);
            c.setPrefHeight(prefH);
            c.setMinHeight(prefH);
        }
        if (n instanceof Region r) {
            r.setPrefHeight(prefH);
            r.setMinHeight(prefH);
        }
        return n;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    //pie + custom legend för 2 kolumner
    private static Node pieWithCustomLegend(String title, Map<String, Long> counts) {

        List<Map.Entry<String, Long>> sorted = counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .toList();

        PieChart pie = new PieChart();
        pie.setTitle(title);

        long sum = sorted.stream().mapToLong(Map.Entry::getValue).sum();

        List<PieChart.Data> dataList = new ArrayList<>();
        for (var e : sorted) {
            PieChart.Data d = new PieChart.Data(e.getKey(), e.getValue());
            pie.getData().add(d);
            dataList.add(d);

            double pct = sum == 0 ? 0 : (100.0 * e.getValue() / sum);
            String tip = e.getKey() + ": " + e.getValue() + " (" + String.format("%.1f%%", pct) + ")";
            d.nodeProperty().addListener((obs, oldN, newN) -> {
                if (newN != null) Tooltip.install(newN, new Tooltip(tip));
            });
        }

        pie.setLegendVisible(false);
        pie.setLabelsVisible(false);

        GridPane legend = new GridPane();
        legend.setHgap(18);
        legend.setVgap(8);
        legend.setPadding(new Insets(6, 4, 0, 4));

        int cols = 2;

        Runnable buildLegend = () -> {
            legend.getChildren().clear();

            for (int i = 0; i < dataList.size(); i++) {
                PieChart.Data d = dataList.get(i);
                long v = (long) d.getPieValue();
                double pct = sum == 0 ? 0 : (100.0 * v / sum);
                String text = d.getName() + " (" + String.format("%.1f%%", pct) + ")";

                Color c = tryReadSliceColor(d);
                if (c == null) c = fallbackPalette(i);

                Region swatch = new Region();
                swatch.setPrefSize(12, 12);
                swatch.setMinSize(12, 12);
                swatch.setMaxSize(12, 12);
                swatch.setBackground(new Background(new BackgroundFill(
                        c, new CornerRadii(3), Insets.EMPTY
                )));
                swatch.setBorder(new Border(new BorderStroke(
                        Color.rgb(0, 0, 0, 0.12),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(3),
                        new BorderWidths(1)
                )));

                Label lbl = new Label(text);
                lbl.setWrapText(true);
                lbl.setMaxWidth(240);

                HBox item = new HBox(8, swatch, lbl);
                item.setAlignment(Pos.CENTER_LEFT);

                int row = i / cols;
                int col = i % cols;
                legend.add(item, col, row);
            }
        };

        Platform.runLater(buildLegend);
        PauseTransition pt = new PauseTransition(Duration.millis(60));
        pt.setOnFinished(e -> buildLegend.run());
        pt.play();

        return new VBox(8, pie, legend);
    }

    private static Color tryReadSliceColor(PieChart.Data d) {
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

        String style = d.getNode().getStyle();
        if (style != null && style.contains("-fx-pie-color:")) {
            try {
                String key = "-fx-pie-color:";
                int idx = style.indexOf(key);
                String rest = style.substring(idx + key.length()).trim();
                int semi = rest.indexOf(";");
                String val = (semi >= 0) ? rest.substring(0, semi).trim() : rest.trim();
                return Color.web(val);
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private static Color fallbackPalette(int i) {
        Color[] palette = new Color[]{
                Color.web("#f3622d"),
                Color.web("#fba71b"),
                Color.web("#57b757"),
                Color.web("#41a9c9"),
                Color.web("#4258c9"),
                Color.web("#9a42c8"),
                Color.web("#c84164"),
                Color.web("#6b6b6b")
        };
        return palette[Math.floorMod(i, palette.length)];
    }

    //shiny pie och stor %
    private static Node shinyPieWithBigPercent(long shiny, long notShiny, double shinyRate) {
        Node pie = pieWithCustomLegend("Shiny vs Not shiny", Map.of(
                "Shiny", shiny,
                "Not shiny", notShiny
        ));

        Label big = new Label(String.format("Shiny: %.1f%%", shinyRate * 100));
        big.setStyle("-fx-font-size: 18px; -fx-font-weight: 700;");

        VBox box = new VBox(8, big, pie);
        box.setAlignment(Pos.TOP_LEFT);
        return box;
    }

    //park och location
    private static Node stackedParkLocation(List<PokemonEntry> entries) {

        Map<String, Map<String, Long>> parkToLoc = new HashMap<>();

        for (PokemonEntry e : entries) {
            String park = safe(e.getPark()).trim();
            String loc = safe(e.getLocation()).trim();
            if (park.isBlank()) park = "Unknown";
            if (loc.isBlank()) loc = "Unknown";

            parkToLoc.computeIfAbsent(park, k -> new HashMap<>());
            Map<String, Long> locMap = parkToLoc.get(park);
            locMap.put(loc, locMap.getOrDefault(loc, 0L) + 1);
        }

        List<String> parks = parkToLoc.entrySet().stream()
                .sorted((a, b) -> Long.compare(totalCount(b.getValue()), totalCount(a.getValue())))
                .map(Map.Entry::getKey)
                .toList();

        Set<String> allLocations = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (var locMap : parkToLoc.values()) allLocations.addAll(locMap.keySet());

        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        y.setLabel("Entries");

        StackedBarChart<String, Number> chart = new StackedBarChart<>(x, y);
        chart.setTitle("Park + Location (stacked)");
        chart.setAnimated(false);
        chart.setLegendVisible(true);

        x.getCategories().setAll(parks);
        x.setTickLabelRotation(45);

        for (String loc : allLocations) {
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.setName(loc);

            for (String park : parks) {
                long val = parkToLoc.getOrDefault(park, Map.of()).getOrDefault(loc, 0L);
                XYChart.Data<String, Number> d = new XYChart.Data<>(park, val);
                s.getData().add(d);

                final String parkF = park;
                final long valF = val;
                d.nodeProperty().addListener((obs, oldN, newN) -> {
                    if (newN != null) {
                        Tooltip.install(newN, new Tooltip("Park: " + parkF + "\nLocation: " + loc + "\nCount: " + valF));
                    }
                });
            }

            chart.getData().add(s);
        }

        return chart;
    }

    private static long totalCount(Map<String, Long> map) {
        return map.values().stream().mapToLong(Long::longValue).sum();
    }

    //catch rate
    private static Node stackedCatchRateByPokemon(Map<String, Long> caughtByPokemon,
                                                  Map<String, Long> notCaughtByPokemon,
                                                  int topN) {

        Map<String, Long> totals = new HashMap<>();
        for (String p : unionKeys(caughtByPokemon, notCaughtByPokemon)) {
            totals.put(p, caughtByPokemon.getOrDefault(p, 0L) + notCaughtByPokemon.getOrDefault(p, 0L));
        }

        List<String> top = totals.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .toList();

        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        y.setLabel("Count");

        StackedBarChart<String, Number> chart = new StackedBarChart<>(x, y);
        chart.setTitle("Catch rate (Top " + topN + " Pokémon)");
        chart.setLegendVisible(true);

        XYChart.Series<String, Number> sCaught = new XYChart.Series<>();
        sCaught.setName("Caught");

        XYChart.Series<String, Number> sNot = new XYChart.Series<>();
        sNot.setName("Not caught");

        for (String p : top) {
            long c = caughtByPokemon.getOrDefault(p, 0L);
            long nc = notCaughtByPokemon.getOrDefault(p, 0L);

            var d1 = new XYChart.Data<String, Number>(p, c);
            var d2 = new XYChart.Data<String, Number>(p, nc);

            sCaught.getData().add(d1);
            sNot.getData().add(d2);

            d1.nodeProperty().addListener((obs, oldN, newN) -> {
                if (newN != null) Tooltip.install(newN, new Tooltip(p + " caught: " + c));
            });
            d2.nodeProperty().addListener((obs, oldN, newN) -> {
                if (newN != null) Tooltip.install(newN, new Tooltip(p + " not caught: " + nc));
            });
        }

        chart.getData().addAll(sCaught, sNot);
        x.setTickLabelRotation(25);

        return chart;
    }

    private static Set<String> unionKeys(Map<String, Long> a, Map<String, Long> b) {
        Set<String> s = new HashSet<>(a.keySet());
        s.addAll(b.keySet());
        return s;
    }

    private static Node lineWithMarkers(String title, Map<LocalDate, Double> series, String yLabel) {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        y.setLabel(yLabel);

        LineChart<String, Number> chart = new LineChart<>(x, y);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        chart.setAnimated(false);

        XYChart.Series<String, Number> s = new XYChart.Series<>();
        series.forEach((day, val) -> s.getData().add(new XYChart.Data<>(day.toString(), val)));

        chart.getData().add(s);
        x.setTickLabelRotation(45);

        for (XYChart.Data<String, Number> d : s.getData()) {
            d.nodeProperty().addListener((obs, oldN, newN) -> {
                if (newN != null) Tooltip.install(newN, new Tooltip(d.getXValue() + ": " + d.getYValue()));
            });
        }

        return chart;
    }

    //bubble chart
    private static Node pokemonBubblesByCaught(Map<String, Long> caughtByPokemon) {

        List<Map.Entry<String, Long>> sorted = caughtByPokemon.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .toList();

        NumberAxis x = new NumberAxis();
        NumberAxis y = new NumberAxis();
        x.setLabel("Pokémon (index)");
        y.setLabel("Caught count");

        BubbleChart<Number, Number> bubble = new BubbleChart<>(x, y);
        bubble.setTitle("Pokémon bubbles (bigger = caught more)");
        bubble.setLegendVisible(false);
        bubble.setAnimated(false);

        XYChart.Series<Number, Number> s = new XYChart.Series<>();

        for (int i = 0; i < sorted.size(); i++) {
            String name = sorted.get(i).getKey();
            long cnt = sorted.get(i).getValue();

            double size = Math.max(1, Math.sqrt(cnt) * 2);

            XYChart.Data<Number, Number> d = new XYChart.Data<>(i + 1, cnt, size);
            s.getData().add(d);

            d.nodeProperty().addListener((obs, oldN, newN) -> {
                if (newN != null) Tooltip.install(newN, new Tooltip(name + " — caught: " + cnt));
            });
        }

        bubble.getData().add(s);
        return bubble;
    }

    private static Map<String, Long> entriesByDayOfWeek(List<PokemonEntry> entries) {
        Map<String, Long> m = new LinkedHashMap<>();
        List<String> days = List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");
        for (String d : days) m.put(cap(d), 0L);

        for (PokemonEntry e : entries) {
            String day = safe(e.getDay()).trim();
            if (day.isBlank()) continue;
            String key = cap(day.toUpperCase());
            m.put(key, m.getOrDefault(key, 0L) + 1);
        }

        if (m.values().stream().mapToLong(Long::longValue).sum() == 0) {
            return Map.of("Entries", (long) entries.size());
        }
        return m;
    }

    private static String cap(String s) {
        if (s == null || s.isBlank()) return "";
        s = s.toLowerCase();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}