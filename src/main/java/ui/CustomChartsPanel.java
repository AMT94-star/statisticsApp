package ui;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.util.Duration;
import model.PokemonEntry;
import ui.charts.ChartRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class CustomChartsPanel {

    private static final String PREF_KEY = "selected_charts_v2";
    private static final String PREF_NODE = "poketracker/charts";

    private static final List<String> DEFAULT_CHARTS = List.of(
            "dow_pie", "catch_rate", "shiny_pie", "weather_pie",
            "park_location", "incense_line"
    );

    private static String draggingId = null;

    public static Node create(FilteredList<PokemonEntry> filtered, boolean tagMode, String namespace) {
        return createInternal(filtered, tagMode, namespace, null);
    }

    public static Node create(FilteredList<PokemonEntry> filtered, boolean tagMode, String namespace, HBox addBtnContainer) {
        return createInternal(filtered, tagMode, namespace, addBtnContainer);
    }

    public static Node create(FilteredList<PokemonEntry> filtered, boolean tagMode) {
        return createInternal(filtered, tagMode, "default", null);
    }

    private static Node createInternal(FilteredList<PokemonEntry> filtered, boolean tagMode, String namespace, HBox addBtnContainer) {

        ObservableList<String> selectedIds = FXCollections.observableArrayList(loadSaved(namespace));

        //graf grid 2kolumner
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        PauseTransition debounce = new PauseTransition(Duration.millis(120));
        debounce.setOnFinished(ev -> rebuildGrid(grid, selectedIds, filtered));

        Runnable scheduleRebuild = () -> {
            debounce.stop();
            debounce.playFromStart();
        };

        filtered.addListener((ListChangeListener<PokemonEntry>) c -> scheduleRebuild.run());
        selectedIds.addListener((ListChangeListener<String>) c -> {
            saveSelected(selectedIds, namespace);
            scheduleRebuild.run();
        });

        //init build
        rebuildGrid(grid, selectedIds, filtered);

        Button addBtn = new Button("Add Chart");
        addBtn.setStyle(
                "-fx-background-color: #3b82f6; -fx-text-fill: white;" +
                        "-fx-font-weight: 700; -fx-font-size: 12px;" +
                        "-fx-background-radius: 7px; -fx-padding: 5 12 5 12; -fx-cursor: hand;"
        );
        addBtn.setOnAction(e -> showAddMenu(selectedIds, tagMode, addBtn));
        if (addBtnContainer != null) {
            addBtnContainer.getChildren().add(addBtn);
        }

        //scroll
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #111827;");

        VBox panel = new VBox(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        panel.setStyle("-fx-background-color: #111827;");

        return panel;
    }

    private static void rebuildGrid(GridPane grid, ObservableList<String> selectedIds,
                                    FilteredList<PokemonEntry> filtered) {
        grid.getChildren().clear();
        grid.getRowConstraints().clear();

        List<String> ids = new ArrayList<>(selectedIds);
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            ChartRegistry.ChartDef def = ChartRegistry.get(id);
            if (def == null) continue;

            Node chart = def.builder().apply(filtered);
            Node card = makeChartCard(id, def.displayName(), chart, selectedIds);

            int row = i / 2;
            int col = i % 2;
            GridPane.setHgrow(card, Priority.ALWAYS);
            GridPane.setFillWidth(card, true);
            grid.add(card, col, row);
        }
    }

    //graf kort med drag och drop
    private static Node makeChartCard(String id, String title, Node chartContent,
                                      ObservableList<String> selectedIds) {


        Button removeBtn = new Button("✕");
        removeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #4b5563;" +
                        "-fx-font-size: 12px; -fx-padding: 0 2 0 2; -fx-cursor: hand;"
        );
        removeBtn.setOnMouseEntered(e -> removeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #ef4444;" +
                        "-fx-font-size: 12px; -fx-padding: 0 2 0 2; -fx-cursor: hand;"
        ));
        removeBtn.setOnMouseExited(e -> removeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #4b5563;" +
                        "-fx-font-size: 12px; -fx-padding: 0 2 0 2; -fx-cursor: hand;"
        ));
        removeBtn.setOnAction(e -> selectedIds.remove(id));

        Label dragHandle = new Label("⠿");
        dragHandle.setStyle(
                "-fx-text-fill: #374151; -fx-font-size: 14px; -fx-cursor: open-hand; -fx-padding: 0 4 0 0;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerRow = new HBox(4, dragHandle, spacer, removeBtn);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setPadding(new Insets(0, 0, 6, 0));

        if (chartContent instanceof Region r) {
            r.setMaxWidth(Double.MAX_VALUE);
        }

        VBox card = new VBox(4, headerRow, chartContent);
        card.setPadding(new Insets(8));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: #1F2937; -fx-background-radius: 10px;");

        card.setOnDragDetected(event -> {
            draggingId = id;
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(id);
            db.setContent(content);
            db.setDragView(card.snapshot(null, null), event.getX(), event.getY());
            event.consume();
        });

        card.setOnDragOver(event -> {
            if (event.getGestureSource() != card && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                card.setStyle("-fx-background-color: #273449; -fx-background-radius: 10px; " +
                        "-fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 10px;");
            }
            event.consume();
        });

        card.setOnDragExited(event ->
                card.setStyle("-fx-background-color: #1F2937; -fx-background-radius: 10px;")
        );

        card.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString() && draggingId != null && !draggingId.equals(id)) {
                int fromIdx = selectedIds.indexOf(draggingId);
                int toIdx = selectedIds.indexOf(id);
                if (fromIdx >= 0 && toIdx >= 0) {
                    selectedIds.remove(fromIdx);
                    selectedIds.add(toIdx, draggingId);
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        card.setOnDragDone(event ->
                card.setStyle("-fx-background-color: #1F2937; -fx-background-radius: 10px;")
        );

        return card;
    }

    //add chart meny
    private static void showAddMenu(ObservableList<String> selectedIds, boolean tagMode, Node anchor) {
        ContextMenu menu = new ContextMenu();
        ChartRegistry.all().forEach((id, def) -> {
            if (id.equals("bubble") && !tagMode) return;
            boolean added = selectedIds.contains(id);
            MenuItem item = new MenuItem((added ? "✓  " : "     ") + def.displayName());
            if (added) {
                item.setDisable(true);
            } else {
                item.setOnAction(ev -> selectedIds.add(id));
            }
            menu.getItems().add(item);
        });
        menu.getItems().add(new SeparatorMenuItem());
        MenuItem reset = new MenuItem("Reset to default");
        reset.setOnAction(ev -> selectedIds.setAll(DEFAULT_CHARTS));
        menu.getItems().add(reset);
        menu.show(anchor, javafx.geometry.Side.BOTTOM, 0, 4);
    }

    private static List<String> loadSaved(String namespace) {
        try {
            String node = PREF_NODE + "/" + namespace;
            Preferences prefs = Preferences.userRoot().node(node);
            String saved = prefs.get(PREF_KEY, null);
            if (saved != null && !saved.isBlank()) {
                List<String> ids = new ArrayList<>(List.of(saved.split(",")));
                ids.removeIf(id -> ChartRegistry.get(id) == null);
                if (!ids.isEmpty()) return ids;
            }
        } catch (Exception ignored) {
        }
        return new ArrayList<>(DEFAULT_CHARTS);
    }

    private static void saveSelected(ObservableList<String> ids, String namespace) {
        try {
            String node = PREF_NODE + "/" + namespace;
            Preferences prefs = Preferences.userRoot().node(node);
            prefs.put(PREF_KEY, String.join(",", ids));
        } catch (Exception ignored) {
        }
    }
}
